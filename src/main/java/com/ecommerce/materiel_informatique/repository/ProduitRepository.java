package com.ecommerce.materiel_informatique.repository;

import com.ecommerce.materiel_informatique.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    // Recherche par catégorie
    List<Produit> findByCategorieId(Long categorieId);

    // Recherche par mot-clé
    List<Produit> findByNomContainingIgnoreCase(String keyword);
}