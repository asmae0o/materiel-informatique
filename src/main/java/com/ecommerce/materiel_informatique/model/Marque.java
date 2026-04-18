package com.ecommerce.materiel_informatique.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Marque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    // Must match the exact variable name in Produit.java ("marque")
    @OneToMany(mappedBy = "marque", cascade = CascadeType.ALL)
    private List<Produit> produits;

    // Default Constructor
    public Marque() {
    }

    // Constructor with parameters
    public Marque(Long id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public List<Produit> getProduits() { return produits; }
    public void setProduits(List<Produit> produits) { this.produits = produits; }
}