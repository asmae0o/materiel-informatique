package com.ecommerce.materiel_informatique.model;

public class CartItem {
    private Produit produit;
    private int quantite;

    public CartItem(Produit produit, int quantite) {
        this.produit = produit;
        this.quantite = quantite;
    }

    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public double getTotalPrice() {
        return produit.getPrix() * quantite;
    }
}