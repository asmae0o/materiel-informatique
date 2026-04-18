package com.ecommerce.materiel_informatique.repository;

import com.ecommerce.materiel_informatique.model.Gerant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GerantRepository extends JpaRepository<Gerant, Long> {
}