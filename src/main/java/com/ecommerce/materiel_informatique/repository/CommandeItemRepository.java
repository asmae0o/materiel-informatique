package com.ecommerce.materiel_informatique.repository;

import com.ecommerce.materiel_informatique.model.CommandeItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeItemRepository extends JpaRepository<CommandeItem, Long> {}
