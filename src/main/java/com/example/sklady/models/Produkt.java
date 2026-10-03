package com.example.sklady.models;

public class Produkt {
    private String id;
    private String nazev;
    private String kategorie;
    private float cena;
    private int mnozstvi;

    public Produkt(String id, String nazev, String kategorie, float cena, int mnozstvi) {
        this.id = id;
        this.nazev = nazev;
        this.kategorie = kategorie;
        this.cena = cena;
        this.mnozstvi = mnozstvi;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNazev() {
        return nazev;
    }

    public void setNazev(String nazev) {
        this.nazev = nazev;
    }

    public String getKategorie() {
        return kategorie;
    }

    public void setKategorie(String kategorie) {
        this.kategorie = kategorie;
    }

    public float getCena() {
        return cena;
    }

    public void setCena(float cena) {
        this.cena = cena;
    }

    public int getMnozstvi() {
        return mnozstvi;
    }

    public void setMnozstvi(int mnozstvi) {
        this.mnozstvi = mnozstvi;
    }
}
