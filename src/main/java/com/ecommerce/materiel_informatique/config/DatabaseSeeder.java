package com.ecommerce.materiel_informatique.config;

import com.ecommerce.materiel_informatique.model.Categorie;
import com.ecommerce.materiel_informatique.model.Marque;
import com.ecommerce.materiel_informatique.model.Produit;
import com.ecommerce.materiel_informatique.repository.CategorieRepository;
import com.ecommerce.materiel_informatique.repository.MarqueRepository;
import com.ecommerce.materiel_informatique.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("h2")
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired private ProduitRepository produitRepository;
    @Autowired private MarqueRepository marqueRepository;
    @Autowired private CategorieRepository categorieRepository;

    @Override
    public void run(String... args) throws Exception {

        // La condition magique : on ne remplit la base que si elle est VIDE !
        if (categorieRepository.count() == 0) {
            System.out.println("⏳ Base de données vide : Injection de données réalistes en cours...");

            // 1. Création des Marques
            Marque apple = new Marque(); apple.setNom("Apple"); marqueRepository.save(apple);
            Marque logitech = new Marque(); logitech.setNom("Logitech"); marqueRepository.save(logitech);
            Marque asus = new Marque(); asus.setNom("Asus"); marqueRepository.save(asus);
            Marque dell = new Marque(); dell.setNom("Dell"); marqueRepository.save(dell);
            Marque nvidia = new Marque(); nvidia.setNom("NVIDIA"); marqueRepository.save(nvidia);

            // 2. Création des Catégories
            Categorie pcPortables = new Categorie(); pcPortables.setNom("PC Portables"); categorieRepository.save(pcPortables);
            Categorie pcBureau = new Categorie(); pcBureau.setNom("PC de Bureau"); categorieRepository.save(pcBureau);
            Categorie peripheriques = new Categorie(); peripheriques.setNom("Périphériques"); categorieRepository.save(peripheriques);
            Categorie composants = new Categorie(); composants.setNom("Composants PC"); categorieRepository.save(composants);

            // 3. Création des Produits Réalistes
            Produit p1 = new Produit();
            p1.setNom("MacBook Pro 14\" M3");
            p1.setDescription("Puce Apple M3, 16 Go RAM, 512 Go SSD. Le summum de la performance pour les créateurs.");
            p1.setPrix(22500.0);
            p1.setQuantiteStock(15);
            p1.setCategorie(pcPortables);
            p1.setMarque(apple);
            produitRepository.save(p1);

            Produit p2 = new Produit();
            p2.setNom("Souris Ergonomique MX Master 3S");
            p2.setDescription("Souris sans fil ultra-précise avec clics silencieux et molette MagSpeed.");
            p2.setPrix(1200.0);
            p2.setQuantiteStock(50);
            p2.setCategorie(peripheriques);
            p2.setMarque(logitech);
            produitRepository.save(p2);

            Produit p3 = new Produit();
            p3.setNom("Carte Graphique GeForce RTX 4070");
            p3.setDescription("12 Go GDDR6X, architecture Ada Lovelace. Idéale pour le gaming en 1440p et la 3D.");
            p3.setPrix(8500.0);
            p3.setQuantiteStock(8);
            p3.setCategorie(composants);
            p3.setMarque(nvidia);
            produitRepository.save(p3);

            Produit p4 = new Produit();
            p4.setNom("Écran Dell UltraSharp 27\" 4K");
            p4.setDescription("Moniteur 4K USB-C, couleurs parfaites pour les designers et développeurs.");
            p4.setPrix(6500.0);
            p4.setQuantiteStock(20);
            p4.setCategorie(peripheriques);
            p4.setMarque(dell);
            produitRepository.save(p4);

            Produit p5 = new Produit();
            p5.setNom("PC Gamer Asus ROG Strix");
            p5.setDescription("Intel Core i9, 32 Go RAM, 1 To NVMe, refroidissement liquide.");
            p5.setPrix(28000.0);
            p5.setQuantiteStock(5);
            p5.setCategorie(pcBureau);
            p5.setMarque(asus);
            produitRepository.save(p5);

            System.out.println("✅ Base de données initialisée avec succès avec un beau catalogue !");
        } else {
            System.out.println("✅ Base de données déjà remplie, on ne touche à rien !");
        }
    }
}