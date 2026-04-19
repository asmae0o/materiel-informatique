package com.ecommerce.materiel_informatique.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String description;
    private double prix;
    private double prixOriginal;
    private int remise;
    private int quantiteStock;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "produit_images", joinColumns = @JoinColumn(name = "produit_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "marque_id")
    private Marque marque;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public double getPrixOriginal() { return prixOriginal; }
    public void setPrixOriginal(double prixOriginal) { this.prixOriginal = prixOriginal; }
    public int getRemise() { return remise; }
    public void setRemise(int remise) { this.remise = remise; }

    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }

    public Marque getMarque() { return marque; }
    public void setMarque(Marque marque) { this.marque = marque; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getPrimaryImage() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }

    public String getImageUrlsJoined() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? String.join("||" , imageUrls) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produit)) return false;
        Produit p = (Produit) o;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
