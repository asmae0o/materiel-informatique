package com.ecommerce.materiel_informatique.service;

import com.ecommerce.materiel_informatique.model.Produit;
import java.util.List;

public interface ProduitService {
    List<Produit> getAllProduits();
    void saveProduit(Produit produit);
    void deleteProduit(Long id);
    Produit getProduitById(Long id);
    List<Produit> searchProduits(String keyword);

    // MÉTHODE AJOUTÉE
    List<Produit> getProduitsByCategorie(Long categorieId);
}