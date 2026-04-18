package com.ecommerce.materiel_informatique.service;

import com.ecommerce.materiel_informatique.model.Marque;
import java.util.List;

public interface MarqueService {
    List<Marque> getAllMarques();
    void saveMarque(Marque marque);
}