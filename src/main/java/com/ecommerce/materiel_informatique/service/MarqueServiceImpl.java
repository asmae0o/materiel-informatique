package com.ecommerce.materiel_informatique.service;

import com.ecommerce.materiel_informatique.model.Marque;
import com.ecommerce.materiel_informatique.repository.MarqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MarqueServiceImpl implements MarqueService {

    @Autowired
    private MarqueRepository marqueRepository;

    @Override
    public List<Marque> getAllMarques() {
        return marqueRepository.findAll();
    }

    @Override
    public void saveMarque(Marque marque) {
        marqueRepository.save(marque);
    }
}