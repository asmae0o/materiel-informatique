package com.ecommerce.materiel_informatique.repository;

import com.ecommerce.materiel_informatique.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findByClientNomOrderByDateCommandeDesc(String clientNom);
}