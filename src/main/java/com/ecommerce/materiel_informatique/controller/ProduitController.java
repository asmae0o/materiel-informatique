package com.ecommerce.materiel_informatique.controller;

import com.ecommerce.materiel_informatique.model.*;
import com.ecommerce.materiel_informatique.service.*;
import com.ecommerce.materiel_informatique.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

@Controller
public class ProduitController {

    @Autowired private ProduitService produitService;
    @Autowired private MarqueService marqueService;
    @Autowired private CategorieService categorieService;
    @Autowired private AvisRepository avisRepository;
    @Autowired private CommandeRepository commandeRepository;
    @Autowired private CommandeItemRepository commandeItemRepository;
    @Autowired private AppUserRepository appUserRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ==========================================
    // --- ROUTES CLIENTS (Catalogue & Profil) ---
    // ==========================================

    @GetMapping("/")
    public String clientInterface(Model model) {
        model.addAttribute("listeProduits", produitService.getAllProduits());
        model.addAttribute("categories", categorieService.getAllCategories());
        return "client_home";
    }

    @GetMapping("/search")
    public String searchCatalogue(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("listeProduits", produitService.searchProduits(keyword));
        model.addAttribute("categories", categorieService.getAllCategories());
        return "client_home";
    }

    @GetMapping("/categorie/{id}")
    public String filterByCategorie(@PathVariable Long id, Model model) {
        model.addAttribute("listeProduits", produitService.getProduitsByCategorie(id));
        model.addAttribute("categories", categorieService.getAllCategories());
        return "client_home";
    }

    @GetMapping("/produit/{id}")
    public String produitDetails(@PathVariable Long id, Model model, Principal principal) {
        Produit produit = produitService.getProduitById(id);
        if (produit == null) return "redirect:/";
        model.addAttribute("produit", produit);
        model.addAttribute("avisList", avisRepository.findByProduitId(id));
        if (principal != null) {
            appUserRepository.findByUsername(principal.getName()).ifPresent(u -> model.addAttribute("currentUser", u));
        }
        
        List<Produit> related = new ArrayList<>();
        if (produit.getCategorie() != null) {
            related = produitService.getProduitsByCategorie(produit.getCategorie().getId())
                    .stream()
                    .filter(p -> !p.getId().equals(id))
                    .limit(4)
                    .toList();
        }
        model.addAttribute("relatedProducts", related);

        return "produit_details";
    }

    @PostMapping("/produit/{id}/avis")
    public String saveAvis(@PathVariable Long id, @RequestParam String clientNom, @RequestParam int note, @RequestParam String commentaire) {
        Produit produit = produitService.getProduitById(id);
        if (produit != null) {
            Avis nouvelAvis = new Avis();
            nouvelAvis.setClientNom(clientNom);
            nouvelAvis.setNote(note);
            nouvelAvis.setCommentaire(commentaire);
            nouvelAvis.setProduit(produit);
            avisRepository.save(nouvelAvis);
        }
        return "redirect:/produit/" + id + "?success";
    }

    @GetMapping("/compte")
    public String monCompte(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : "";
        model.addAttribute("commandes", commandeRepository.findByClientNomOrderByDateCommandeDesc(username));
        appUserRepository.findByUsername(username).ifPresent(u -> model.addAttribute("currentUser", u));
        return "client_compte";
    }

    @PostMapping("/compte/update")
    public String updateProfil(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telephone,
            Principal principal) {
        if (principal != null) {
            appUserRepository.findByUsername(principal.getName()).ifPresent(u -> {
                u.setNom(nom != null && !nom.isBlank() ? nom.trim() : null);
                u.setEmail(email != null && !email.isBlank() ? email.trim() : null);
                u.setTelephone(telephone != null && !telephone.isBlank() ? telephone.trim() : null);
                appUserRepository.save(u);
            });
        }
        return "redirect:/compte?infoSuccess";
    }

    @PostMapping("/compte/update-address")
    public String updateAddress(@RequestParam(required = false) String adresse, Principal principal) {
        if (principal != null) {
            appUserRepository.findByUsername(principal.getName()).ifPresent(u -> {
                u.setAdresse(adresse != null && !adresse.isBlank() ? adresse.trim() : null);
                appUserRepository.save(u);
            });
        }
        return "redirect:/compte?addressSuccess";
    }

    @PostMapping("/compte/update-password")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Principal principal,
            RedirectAttributes ra) {
        if (principal == null) return "redirect:/compte";
        var userOpt = appUserRepository.findByUsername(principal.getName());
        if (userOpt.isEmpty()) return "redirect:/compte";
        var u = userOpt.get();
        if (!passwordEncoder.matches(currentPassword, u.getPassword())) {
            return "redirect:/compte?pwdError=incorrect";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "redirect:/compte?pwdError=mismatch";
        }
        u.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(u);
        return "redirect:/compte?pwdSuccess";
    }

    @GetMapping("/compte/commande/{id}")
    public String detailsCommande(@PathVariable Long id, Model model) {
        Commande commande = commandeRepository.findById(id).orElse(null);
        if (commande == null) return "redirect:/compte";
        model.addAttribute("commande", commande);
        return "commande_details";
    }

    // ==========================================
    // --- ROUTES PANIER & CHECKOUT ---
    // ==========================================

    @GetMapping("/panier")
    public String voirPanier() {
        return "redirect:/";
    }

    @GetMapping("/cart/panel")
    public String cartPanel(Model model, HttpSession session, Principal principal) {
        fillCartModel(model, session, principal);
        return "fragments/cart_panel :: cartPanelContent";
    }

    @GetMapping("/cart/count")
    @ResponseBody
    public int cartCount(HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) return 0;
        return cart.values().stream().mapToInt(CartItem::getQuantite).sum();
    }

    @PostMapping("/cart/add/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> addToCart(@PathVariable Long id, HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        Produit p = produitService.getProduitById(id);
        if (p != null) {
            if (cart.containsKey(id)) {
                cart.get(id).setQuantite(cart.get(id).getQuantite() + 1);
            } else {
                cart.put(id, new CartItem(p, 1));
            }
        }
        return org.springframework.http.ResponseEntity.ok("ok");
    }

    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean panel, Model model, HttpSession session, Principal principal) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart != null) cart.remove(id);

        if (panel) {
            fillCartModel(model, session, principal);
            return "fragments/cart_panel :: cartPanelContent";
        }

        return "redirect:/";
    }

    @GetMapping("/cart/add-qty/{id}")
    public String addQuantity(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean panel, Model model, HttpSession session, Principal principal) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart != null && cart.containsKey(id)) cart.get(id).setQuantite(cart.get(id).getQuantite() + 1);

        if (panel) {
            fillCartModel(model, session, principal);
            return "fragments/cart_panel :: cartPanelContent";
        }

        return "redirect:/";
    }

    @GetMapping("/cart/min-qty/{id}")
    public String minQuantity(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean panel, Model model, HttpSession session, Principal principal) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart != null && cart.containsKey(id)) {
            CartItem item = cart.get(id);
            if (item.getQuantite() > 1) item.setQuantite(item.getQuantite() - 1);
            else cart.remove(id);
        }

        if (panel) {
            fillCartModel(model, session, principal);
            return "fragments/cart_panel :: cartPanelContent";
        }

        return "redirect:/";
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model, HttpSession session, Principal principal) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) return "redirect:/";
        double total = cart.values().stream().mapToDouble(CartItem::getTotalPrice).sum();
        model.addAttribute("total", total);
        model.addAttribute("cartItems", cart.values());
        if (principal != null) {
            appUserRepository.findByUsername(principal.getName()).ifPresent(u -> {
                model.addAttribute("currentUserNom", u.getNom() != null ? u.getNom() : u.getUsername());
                model.addAttribute("currentUserTel", u.getTelephone() != null ? u.getTelephone() : "");
                model.addAttribute("currentUserAdresse", u.getAdresse() != null ? u.getAdresse() : "");
            });
        }
        return "checkout";
    }

    private void fillCartModel(Model model, HttpSession session, Principal principal) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        double total = cart.values().stream().mapToDouble(CartItem::getTotalPrice).sum();
        int itemCount = cart.values().stream().mapToInt(CartItem::getQuantite).sum();

        model.addAttribute("cartItems", cart.values());
        model.addAttribute("total", total);
        model.addAttribute("itemCount", itemCount);
        model.addAttribute("isAuthenticated", principal != null);
    }

    @PostMapping("/cart/validate")
    public String validateOrder(@RequestParam String nom, @RequestParam String adresse, @RequestParam String paiement, HttpSession session, Principal principal) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart != null && !cart.isEmpty()) {
            double total = cart.values().stream().mapToDouble(CartItem::getTotalPrice).sum();
            Commande cmd = new Commande();
            cmd.setNumero("#CMD-" + (1000 + new Random().nextInt(9000)));
            cmd.setDateCommande(LocalDate.now());
            cmd.setTotal(total);
            cmd.setStatut("En cours de préparation");
            cmd.setAdresseLivraison(adresse);
            cmd.setMethodePaiement(paiement);
            cmd.setClientNom(principal != null ? principal.getName() : nom);
            commandeRepository.save(cmd);
            for (CartItem item : cart.values()) {
                commandeItemRepository.save(new CommandeItem(cmd, item.getProduit(), item.getQuantite(), item.getProduit().getPrix()));
                Produit p = item.getProduit();
                p.setQuantiteStock(Math.max(0, p.getQuantiteStock() - item.getQuantite()));
                produitService.saveProduit(p);
            }
            session.removeAttribute("cart");
            if (principal != null) {
                appUserRepository.findByUsername(principal.getName()).ifPresent(u -> {
                    u.setAdresse(adresse);
                    appUserRepository.save(u);
                });
            }
        }
        return "redirect:/?orderSuccess";
    }

    // ==========================================
    // --- ROUTES GÉRANT (Gestion du Catalogue) ---
    // ==========================================

    @GetMapping("/gerant/dashboard")
    public String gerantInterface(Model model) {
        model.addAttribute("produits", produitService.getAllProduits());
        model.addAttribute("marques", marqueService.getAllMarques());
        model.addAttribute("categories", categorieService.getAllCategories());
        return "gerant_products";
    }

    @GetMapping("/gerant/products")
    public String gerantProducts(Model model) {
        model.addAttribute("produits", produitService.getAllProduits());
        model.addAttribute("marques", marqueService.getAllMarques());
        model.addAttribute("categories", categorieService.getAllCategories());
        return "gerant_products";
    }

    @PostMapping("/gerant/products/save")
    public String gerantSaveProduct(
            @RequestParam String nom,
            @RequestParam(required = false) String description,
            @RequestParam double prix,
            @RequestParam int quantiteStock,
            @RequestParam Long marque,
            @RequestParam Long categorie) {
        Produit p = new Produit();
        p.setNom(nom);
        p.setDescription(description);
        p.setPrix(prix);
        p.setQuantiteStock(quantiteStock);
        marqueService.getAllMarques().stream().filter(m -> m.getId().equals(marque)).findFirst().ifPresent(p::setMarque);
        categorieService.getAllCategories().stream().filter(c -> c.getId().equals(categorie)).findFirst().ifPresent(p::setCategorie);
        produitService.saveProduit(p);
        return "redirect:/gerant/products";
    }

    @PostMapping("/gerant/products/update")
    public String gerantUpdateProduct(
            @RequestParam Long id,
            @RequestParam String nom,
            @RequestParam(required = false) String description,
            @RequestParam double prix,
            @RequestParam int quantiteStock,
            @RequestParam Long marque,
            @RequestParam Long categorie) {
        Produit p = produitService.getProduitById(id);
        if (p != null) {
            p.setNom(nom);
            p.setDescription(description);
            p.setPrix(prix);
            p.setQuantiteStock(quantiteStock);
            marqueService.getAllMarques().stream().filter(m -> m.getId().equals(marque)).findFirst().ifPresent(p::setMarque);
            categorieService.getAllCategories().stream().filter(c -> c.getId().equals(categorie)).findFirst().ifPresent(p::setCategorie);
            produitService.saveProduit(p);
        }
        return "redirect:/gerant/products";
    }

    @GetMapping("/gerant/commandes")
    public String gerantCommandes(Model model) {
        model.addAttribute("commandes", commandeRepository.findAll());
        return "gerant_commandes";
    }

    @PostMapping("/gerant/commandes/statut/{id}")
    public String gerantUpdateStatut(@PathVariable Long id, @RequestParam String statut) {
        commandeRepository.findById(id).ifPresent(cmd -> {
            cmd.setStatut(statut);
            commandeRepository.save(cmd);
        });
        return "redirect:/gerant/commandes?success";
    }

    @GetMapping("/gerant/avis")
    public String gerantAvis(Model model) {
        model.addAttribute("avisList", avisRepository.findAll());
        return "gerant_avis";
    }

    @GetMapping("/gerant/nouveau")
    public String showAddForm(Model model) {
        model.addAttribute("produit", new Produit());
        model.addAttribute("marques", marqueService.getAllMarques());
        model.addAttribute("categories", categorieService.getAllCategories());
        model.addAttribute("formAction", "/gerant/save");
        model.addAttribute("cancelUrl", "/gerant/dashboard");
        return "form_produit";
    }

    @PostMapping("/gerant/save")
    public String saveProduit(@ModelAttribute("produit") Produit produit,
                              @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        // For edits, preserve existing images
        if (produit.getId() != null) {
            Produit existing = produitService.getProduitById(produit.getId());
            if (existing != null && produit.getImageUrls().isEmpty()) {
                produit.setImageUrls(new ArrayList<>(new java.util.LinkedHashSet<>(existing.getImageUrls())));
            }
        }
        saveImages(produit, images);
        produitService.saveProduit(produit);
        return "redirect:/gerant/dashboard";
    }

    @GetMapping("/gerant/delete/{id}")
    public String deleteProduit(@PathVariable Long id) {
        produitService.deleteProduit(id);
        return "redirect:/gerant/dashboard";
    }

    @GetMapping("/gerant/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categorieService.getAllCategories());
        return "gerant_categories";
    }

    @PostMapping("/gerant/categories/save")
    public String saveCategorie(@RequestParam("nom") String nom) {
        Categorie cat = new Categorie();
        cat.setNom(nom);
        categorieService.saveCategorie(cat);
        return "redirect:/gerant/categories";
    }

    @PostMapping("/gerant/categories/update")
    public String updateCategorie(@RequestParam Long id, @RequestParam String nom) {
        categorieService.getAllCategories().stream()
            .filter(c -> c.getId().equals(id)).findFirst().ifPresent(c -> {
                c.setNom(nom);
                categorieService.saveCategorie(c);
            });
        return "redirect:/gerant/categories";
    }

    @GetMapping("/gerant/categories/delete/{id}")
    public String deleteCategorie(@PathVariable Long id) {
        categorieService.deleteCategorie(id);
        return "redirect:/gerant/categories";
    }

    @GetMapping("/gerant/promotions")
    public String viewPromotions(Model model) {
        model.addAttribute("produits", produitService.getAllProduits());
        return "gerant_promotions";
    }

    @PostMapping("/gerant/promotions/apply")
    public String applyPromotion(@RequestParam Long produitId, @RequestParam int remise) {
        Produit p = produitService.getProduitById(produitId);
        if (p != null) {
            double base = p.getPrixOriginal() > 0 ? p.getPrixOriginal() : p.getPrix();
            p.setPrixOriginal(base);
            p.setRemise(remise);
            p.setPrix(base * (1 - remise / 100.0));
            produitService.saveProduit(p);
            return "redirect:/gerant/promotions?success";
        }
        return "redirect:/gerant/promotions?error";
    }

    // ==========================================
    // --- ROUTES SUPER ADMIN (Control Center) ---
    // ==========================================

    @GetMapping("/admin/dashboard")
    public String adminInterface() {
        return "admin_dashboard";
    }

    @GetMapping("/admin/gerants")
    public String manageGerants(@RequestParam(required = false) String role, Model model) {
        var users = appUserRepository.findAll().stream()
                .filter(u -> !u.getRole().equals("ADMIN"))
                .filter(u -> role == null || u.getRole().equals(role))
                .toList();
        model.addAttribute("users", users);
        model.addAttribute("activeFilter", role);
        return "admin_users";
    }

    @GetMapping("/admin/users")
    public String manageUsers(@RequestParam(required = false) String role, Model model) {
        return manageGerants(role, model);
    }

    @PostMapping("/admin/gerants/save")
    public String saveGerant(@RequestParam String username, @RequestParam String password,
                             @RequestParam String role, RedirectAttributes redirectAttributes) {
        String clean = username == null ? "" : username.trim().toLowerCase();
        if (appUserRepository.existsByUsername(clean)) {
            redirectAttributes.addFlashAttribute("userError", "Ce nom d'utilisateur existe déjà.");
            return "redirect:/admin/gerants";
        }
        appUserRepository.save(new AppUser(clean, passwordEncoder.encode(password), role));
        return "redirect:/admin/gerants";
    }

    @PostMapping("/admin/users/save")
    public String saveUser(@RequestParam String username, @RequestParam String password,
                           @RequestParam String role, RedirectAttributes redirectAttributes) {
        return saveGerant(username, password, role, redirectAttributes);
    }

    @PostMapping("/admin/gerants/update")
    public String adminUpdateGerant(@RequestParam Long id, @RequestParam String username,
                                    @RequestParam String role,
                                    @RequestParam(required = false) String nom,
                                    @RequestParam(required = false) String email,
                                    @RequestParam(required = false) String telephone) {
        appUserRepository.findById(id).ifPresent(u -> {
            u.setUsername(username != null ? username.trim().toLowerCase() : u.getUsername());
            u.setRole(role);
            u.setNom(nom != null && !nom.isBlank() ? nom.trim().toLowerCase() : null);
            u.setEmail(email != null && !email.isBlank() ? email.trim().toLowerCase() : null);
            u.setTelephone(telephone != null && !telephone.isBlank() ? telephone.trim() : null);
            appUserRepository.save(u);
        });
        return "redirect:/admin/gerants";
    }

    @GetMapping("/admin/gerants/delete/{id}")
    public String deleteGerant(@PathVariable Long id) {
        appUserRepository.deleteById(id);
        return "redirect:/admin/gerants";
    }

    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        appUserRepository.deleteById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/avis")
    public String manageAvis(Model model) {
        model.addAttribute("avisList", avisRepository.findAll());
        return "admin_avis";
    }

    @GetMapping("/admin/avis/delete/{id}")
    public String deleteAvis(@PathVariable Long id) {
        avisRepository.deleteById(id);
        return "redirect:/admin/avis";
    }

    @GetMapping("/admin/commandes")
    public String viewAllCommandes(Model model) {
        model.addAttribute("commandes", commandeRepository.findAll());
        return "admin_commandes";
    }

    @GetMapping("/admin/commandes/{id}")
    public String commandeDetails(@PathVariable Long id, Model model) {
        commandeRepository.findById(id).ifPresent(cmd -> {
            model.addAttribute("cmd", cmd);
            appUserRepository.findByUsername(cmd.getClientNom()).ifPresent(u -> model.addAttribute("client", u));
        });
        return "admin_commande_details";
    }

    @PostMapping("/admin/commandes/statut/{id}")
    public String updateCommandeStatut(@PathVariable Long id, @RequestParam String statut) {
        commandeRepository.findById(id).ifPresent(cmd -> {
            cmd.setStatut(statut);
            commandeRepository.save(cmd);
        });
        return "redirect:/admin/commandes/" + id;
    }

    @GetMapping("/admin/products")
    public String adminProducts(Model model) {
        model.addAttribute("produits", produitService.getAllProduits());
        model.addAttribute("marques", marqueService.getAllMarques());
        model.addAttribute("categories", categorieService.getAllCategories());
        return "admin_products";
    }

    @GetMapping("/admin/products/new")
    public String adminNewProduct(Model model) {
        model.addAttribute("produit", new Produit());
        model.addAttribute("marques", marqueService.getAllMarques());
        model.addAttribute("categories", categorieService.getAllCategories());
        model.addAttribute("formAction", "/admin/products/save");
        model.addAttribute("cancelUrl", "/admin/products");
        return "form_produit";
    }

    @GetMapping("/admin/products/edit/{id}")
    public String adminEditProduct(@PathVariable Long id, Model model) {
        Produit produit = produitService.getProduitById(id);
        if (produit == null) return "redirect:/admin/products";
        model.addAttribute("produit", produit);
        model.addAttribute("marques", marqueService.getAllMarques());
        model.addAttribute("categories", categorieService.getAllCategories());
        model.addAttribute("formAction", "/admin/products/save");
        model.addAttribute("cancelUrl", "/admin/products");
        return "form_produit";
    }

    @PostMapping("/admin/products/save")
    public String adminSaveProduct(@ModelAttribute("produit") Produit produit,
                                   @RequestParam(value = "images", required = false) List<MultipartFile> images,
                                   @RequestParam(value = "primaryImageUrl", required = false) String primaryImageUrl,
                                   @RequestParam(value = "imageOrder", required = false) String imageOrder) throws IOException {
        // For edits, preserve existing images
        if (produit.getId() != null) {
            Produit existing = produitService.getProduitById(produit.getId());
            if (existing != null && produit.getImageUrls().isEmpty()) {
                produit.setImageUrls(new ArrayList<>(new java.util.LinkedHashSet<>(existing.getImageUrls())));
            }
        }

        // Apply drag-and-drop image order (also handles deletions — only listed URLs are kept)
        if (imageOrder != null && !imageOrder.isBlank()) {
            List<String> current = produit.getImageUrls();
            List<String> reordered = new ArrayList<>();
            for (String url : imageOrder.split("\\|\\|")) {
                String u = url.trim();
                if (!u.isEmpty() && current.contains(u)) reordered.add(u);
            }
            produit.setImageUrls(reordered);
        } else if (primaryImageUrl != null && !primaryImageUrl.isBlank()) {
            // Fallback: just move primary to front
            List<String> urls = produit.getImageUrls();
            if (urls.remove(primaryImageUrl)) urls.add(0, primaryImageUrl);
        }

        // Append new uploads after the ordered list
        saveImages(produit, images);
        produitService.saveProduit(produit);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/delete/{id}")
    public String adminDeleteProduct(@PathVariable Long id) {
        produitService.deleteProduit(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/categories")
    public String adminCategories(Model model) {
        model.addAttribute("categories", categorieService.getAllCategories());
        return "admin_categories";
    }

    @PostMapping("/admin/categories/save")
    public String adminSaveCategorie(@RequestParam("nom") String nom) {
        Categorie cat = new Categorie();
        cat.setNom(nom);
        categorieService.saveCategorie(cat);
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/edit/{id}")
    public String adminEditCategorieForm(@PathVariable Long id, Model model) {
        model.addAttribute("categories", categorieService.getAllCategories());
        model.addAttribute("editCategorie", categorieService.getCategorieById(id));
        return "admin_categories";
    }

    @PostMapping("/admin/categories/update")
    public String adminUpdateCategorie(@RequestParam Long id, @RequestParam String nom) {
        Categorie cat = categorieService.getCategorieById(id);
        if (cat != null) {
            cat.setNom(nom);
            categorieService.saveCategorie(cat);
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/delete/{id}")
    public String adminDeleteCategorie(@PathVariable Long id) {
        categorieService.deleteCategorie(id);
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/promotions")
    public String adminPromotions(Model model) {
        model.addAttribute("produits", produitService.getAllProduits());
        return "admin_promotions";
    }

    @PostMapping("/admin/promotions/apply")
    public String adminApplyPromotion(@RequestParam Long produitId, @RequestParam int remise) {
        Produit p = produitService.getProduitById(produitId);
        if (p != null) {
            double base = p.getPrixOriginal() > 0 ? p.getPrixOriginal() : p.getPrix();
            p.setPrixOriginal(base);
            p.setRemise(remise);
            p.setPrix(base * (1 - remise / 100.0));
            produitService.saveProduit(p);
            return "redirect:/admin/promotions?success";
        }
        return "redirect:/admin/promotions?error";
    }
    // ==========================================
    // --- ROUTE POUR LA PAGE DE CONNEXION ---
    // ==========================================
    @GetMapping("/login")
    public String showLoginForm() {
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String showSignupForm() {
        return "redirect:/";
    }

    @PostMapping("/signup")
    public String handleSignup(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telephone,
            RedirectAttributes redirectAttributes) {

        String cleanUsername = username == null ? "" : username.trim().toLowerCase();

        if (cleanUsername.isEmpty() || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("signupError", "Nom d'utilisateur et mot de passe sont obligatoires.");
            return "redirect:/?signupError";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("signupError", "Les mots de passe ne correspondent pas.");
            return "redirect:/?signupError";
        }

        if (appUserRepository.existsByUsername(cleanUsername)) {
            redirectAttributes.addFlashAttribute("signupError", "Ce nom d'utilisateur existe déjà.");
            return "redirect:/?signupError";
        }

        AppUser newUser = new AppUser(cleanUsername, passwordEncoder.encode(password), "CLIENT");
        newUser.setNom(nom != null && !nom.isBlank() ? nom.trim().toLowerCase() : null);
        newUser.setEmail(email != null && !email.isBlank() ? email.trim().toLowerCase() : null);
        newUser.setTelephone(telephone != null && !telephone.isBlank() ? telephone.trim() : null);
        appUserRepository.save(newUser);

        return "redirect:/";
    }

    private void saveImages(Produit produit, List<MultipartFile> images) throws IOException {
        if (images == null || images.isEmpty()) return;
        if (produit.getImageUrls() == null) {
            produit.setImageUrls(new ArrayList<>());
        }
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);
        List<String> newUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Files.copy(image.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                newUrls.add("/uploads/" + filename);
            }
        }
        if (!newUrls.isEmpty()) {
            produit.getImageUrls().addAll(0, newUrls);
        }
    }
}