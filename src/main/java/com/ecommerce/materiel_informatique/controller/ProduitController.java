package com.ecommerce.materiel_informatique.controller;

import com.ecommerce.materiel_informatique.model.*;
import com.ecommerce.materiel_informatique.service.*;
import com.ecommerce.materiel_informatique.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ProduitController {

    @Autowired private ProduitService produitService;
    @Autowired private MarqueService marqueService;
    @Autowired private CategorieService categorieService;
    @Autowired private GerantRepository gerantRepository;
    @Autowired private AvisRepository avisRepository;
    @Autowired private CommandeRepository commandeRepository;
    @Autowired private InMemoryUserDetailsManager userDetailsManager;
    private final Set<String> clientUsers = ConcurrentHashMap.newKeySet();

    public ProduitController() {
        // Default client account available at startup
        clientUsers.add("asmae");
    }

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
    public String produitDetails(@PathVariable Long id, Model model) {
        Produit produit = produitService.getProduitById(id);
        if (produit == null) return "redirect:/";
        model.addAttribute("produit", produit);
        model.addAttribute("avisList", avisRepository.findByProduitId(id));
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
        String username = principal != null ? principal.getName() : "Asmae";
        model.addAttribute("commandes", commandeRepository.findByClientNomOrderByDateCommandeDesc(username));
        return "client_compte";
    }

    @PostMapping("/compte/update")
    public String updateProfil(@RequestParam String nom, @RequestParam String adresse) {
        return "redirect:/compte?success";
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
    public String cartPanel(Model model, HttpSession session) {
        fillCartModel(model, session);
        return "fragments/cart_panel :: cartPanelContent";
    }

    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session) {
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
        return "redirect:/?cartSuccess";
    }

    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean panel, Model model, HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart != null) cart.remove(id);

        if (panel) {
            fillCartModel(model, session);
            return "fragments/cart_panel :: cartPanelContent";
        }

        return "redirect:/";
    }

    @GetMapping("/cart/add-qty/{id}")
    public String addQuantity(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean panel, Model model, HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart != null && cart.containsKey(id)) cart.get(id).setQuantite(cart.get(id).getQuantite() + 1);

        if (panel) {
            fillCartModel(model, session);
            return "fragments/cart_panel :: cartPanelContent";
        }

        return "redirect:/";
    }

    @GetMapping("/cart/min-qty/{id}")
    public String minQuantity(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean panel, Model model, HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart != null && cart.containsKey(id)) {
            CartItem item = cart.get(id);
            if (item.getQuantite() > 1) item.setQuantite(item.getQuantite() - 1);
            else cart.remove(id);
        }

        if (panel) {
            fillCartModel(model, session);
            return "fragments/cart_panel :: cartPanelContent";
        }

        return "redirect:/";
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model, HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) return "redirect:/";
        double total = cart.values().stream().mapToDouble(CartItem::getTotalPrice).sum();
        model.addAttribute("total", total);
        return "checkout";
    }

    private void fillCartModel(Model model, HttpSession session) {
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
            session.removeAttribute("cart");
        }
        return "redirect:/order-confirmation";
    }

    @GetMapping("/order-confirmation")
    public String orderConfirmation() {
        return "order_success";
    }

    // ==========================================
    // --- ROUTES GÉRANT (Gestion du Catalogue) ---
    // ==========================================

    @GetMapping("/gerant/dashboard")
    public String gerantInterface(Model model) {
        model.addAttribute("produits", produitService.getAllProduits());
        return "admin_products";
    }

    @GetMapping("/gerant/nouveau")
    public String showAddForm(Model model) {
        model.addAttribute("produit", new Produit());
        model.addAttribute("marques", marqueService.getAllMarques());
        model.addAttribute("categories", categorieService.getAllCategories());
        return "form_produit";
    }

    @PostMapping("/gerant/save")
    public String saveProduit(@ModelAttribute("produit") Produit produit) {
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
    public String applyPromotion(@RequestParam Long produitId, @RequestParam Double remise) {
        Produit p = produitService.getProduitById(produitId);
        if (p != null) {
            double nouveauPrix = p.getPrix() * (1 - (remise / 100));
            p.setPrix(nouveauPrix);
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
    public String manageGerants(Model model) {
        model.addAttribute("gerants", gerantRepository.findAll());
        model.addAttribute("clients", clientUsers.stream().sorted().toList());
        return "admin_users";
    }

    @GetMapping("/admin/users")
    public String manageUsers(Model model) {
        return manageGerants(model);
    }

    @PostMapping("/admin/gerants/save")
    public String saveGerant(@RequestParam("nom") String nom, @RequestParam("email") String email) {
        Gerant g = new Gerant(nom, email);
        gerantRepository.save(g);
        return "redirect:/admin/gerants";
    }

    @PostMapping("/admin/users/save")
    public String saveUser(@RequestParam("nom") String nom, @RequestParam("email") String email) {
        Gerant g = new Gerant(nom, email);
        gerantRepository.save(g);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/gerants/delete/{id}")
    public String deleteGerant(@PathVariable Long id) {
        gerantRepository.deleteById(id);
        return "redirect:/admin/gerants";
    }

    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        gerantRepository.deleteById(id);
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
    public String viewAllCommandes() {
        return "admin_commandes";
    }

    @GetMapping("/admin/products")
    public String adminProducts(Model model) {
        model.addAttribute("produits", produitService.getAllProduits());
        return "admin_products";
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
    public String adminApplyPromotion(@RequestParam Long produitId, @RequestParam Double remise) {
        Produit p = produitService.getProduitById(produitId);
        if (p != null) {
            double nouveauPrix = p.getPrix() * (1 - (remise / 100));
            p.setPrix(nouveauPrix);
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
        return "login"; // Va chercher le fichier login.html
    }

    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignup(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        String cleanUsername = username == null ? "" : username.trim();

        if (cleanUsername.isEmpty() || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("signupError", "Nom d'utilisateur et mot de passe sont obligatoires.");
            return "redirect:/signup";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("signupError", "Les mots de passe ne correspondent pas.");
            return "redirect:/signup";
        }

        if (userDetailsManager.userExists(cleanUsername)) {
            redirectAttributes.addFlashAttribute("signupError", "Ce nom d'utilisateur existe deja.");
            return "redirect:/signup";
        }

        userDetailsManager.createUser(User.withUsername(cleanUsername)
                .password("{noop}" + password)
                .roles("CLIENT")
                .build());

        clientUsers.add(cleanUsername);

        return "redirect:/login?registered";
    }
}