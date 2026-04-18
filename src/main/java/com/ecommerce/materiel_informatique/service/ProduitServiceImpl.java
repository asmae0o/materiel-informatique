package com.ecommerce.materiel_informatique.service;

import com.ecommerce.materiel_informatique.model.Produit;
import com.ecommerce.materiel_informatique.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List; // TRÈS IMPORTANT : Utilisez java.util.List

@Service
public class ProduitServiceImpl implements ProduitService {

    @Autowired
    private ProduitRepository produitRepository;

    @Override
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    @Override
    public void saveProduit(Produit produit) {
        produitRepository.save(produit);
    }

    @Override
    public void deleteProduit(Long id) {
        produitRepository.deleteById(id);
    }

    @Override
    public Produit getProduitById(Long id) {
        return produitRepository.findById(id).orElse(null);
    }

    @Override
    public List<Produit> searchProduits(String keyword) {
        return produitRepository.findByNomContainingIgnoreCase(keyword);
    }

    // MÉTHODE AJOUTÉE AVEC @Override
    @Override
    public List<Produit> getProduitsByCategorie(Long categorieId) {
        return produitRepository.findByCategorieId(categorieId);
    }
}