package com.ecommerce.materiel_informatique.service;

import com.ecommerce.materiel_informatique.model.Categorie;
import java.util.List;

public interface CategorieService {
    List<Categorie> getAllCategories();
    void saveCategorie(Categorie categorie);
    void deleteCategorie(Long id);
    Categorie getCategorieById(Long id);
}