package com.ecommerce.materiel_informatique.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String description;
    private double prix;
    private int quantiteStock;
     // Contiendra par exemple "macbook.png"

    @ManyToOne
    @JoinColumn(name = "marque_id")
    private Marque marque;

    // Fixed: Single Categorie, not a List
    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }

    public Marque getMarque() { return marque; }
    public void setMarque(Marque marque) { this.marque = marque; }

    // Fixed Getter/Setter name
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
}