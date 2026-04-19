package com.ecommerce.materiel_informatique.model;

public class Gerant {
    private Long id;
    private String nom;
    private String email;
    private String statut = "Actif";
    private String username;

    public Gerant() {}
    public Gerant(String nom, String email, String username) {
        this.nom = nom;
        this.email = email;
        this.username = username;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
