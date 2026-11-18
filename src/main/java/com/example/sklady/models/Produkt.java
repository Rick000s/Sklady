package com.example.sklady.models;

import com.example.sklady.enums.enumSklad;

public class Produkt {
    private final String id;
    private String nazev;
    private String kategorie;
    private float cena;
    private int mnozstvi;
    private enumSklad sklad;

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

    public enumSklad getSklad() {
        return sklad;
    }

    public void setSklad(enumSklad sklad) {
        this.sklad = sklad;
    }
}
