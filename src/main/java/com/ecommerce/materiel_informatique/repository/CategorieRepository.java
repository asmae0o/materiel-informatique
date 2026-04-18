package com.ecommerce.materiel_informatique.repository;

import com.ecommerce.materiel_informatique.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {
}