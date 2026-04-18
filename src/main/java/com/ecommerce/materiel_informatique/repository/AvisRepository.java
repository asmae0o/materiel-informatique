package com.ecommerce.materiel_informatique.repository;

import com.ecommerce.materiel_informatique.model.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {
    // Cette ligne permet de trouver les avis d'un produit spécifique
    List<Avis> findByProduitId(Long produitId);
}