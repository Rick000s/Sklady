package com.example.sklady.managers;

import com.example.sklady.datastructures.AbstrFIFO;
import com.example.sklady.datastructures.AbstrTable;
import com.example.sklady.enums.eTypProhl;
import com.example.sklady.enums.enumPozice;
import com.example.sklady.enums.enumSklad;
import com.example.sklady.models.Produkt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class AgendaSklady {

    private static final int MAX_CISLO_ID = 999;

    private static final String[] NAZVY = {
            "Notebook", "Monitor", "Klavesnice", "Mys", "Tiskarna", "Toner", "Papir A4",
            "Zidle", "Stul", "Lampa", "Kabel", "Baterie", "Sroubovak", "Vrtacka", "Rukavice"
    };

    private static final String[] KATEGORIE = {
            "Elektronika", "Spotrebni material", "Nabytek", "Naradi", "Elektro", "Hygiena"
    };

    private final Sklady sklady = new Sklady();
    private final AbstrTable<String, Produkt> tabulka = new AbstrTable<>();
    private final Random random = new Random();
    private int pocet = 0;

    public Produkt najdi(String id) {
        return tabulka.najdi(id);
    }

    public boolean vloz(Produkt produkt, enumPozice pozice, enumSklad sklad) {
        if (produkt == null || pozice == null || sklad == null) return false;

        String id = produkt.getId();
        if (id == null || id.isBlank()) return false;
        if (tabulka.najdi(id) != null) return false;

        produkt.setSklad(sklad);
        sklady.vlozProdukt(produkt, pozice, sklad);
        tabulka.vloz(id, produkt);
        pocet++;
        return true;
    }

    public Produkt odeber(String id) {
        Produkt produkt = tabulka.odeber(id);
        if (produkt == null) return null;

        sklady.odeberProduktPodleId(produkt.getSklad(), id);
        pocet--;
        return produkt;
    }

    public boolean presun(String id, enumSklad cil) {
        Produkt produkt = tabulka.najdi(id);
        if (produkt == null || cil == null) return false;

        enumSklad zdroj = produkt.getSklad();
        if (zdroj == cil) return false;
        if (!sklady.synchronizujAktualniPodleId(zdroj, id)) return false;

        sklady.presunProdukt(zdroj, cil);
        produkt.setSklad(cil);
        return true;
    }

    public Produkt[] najdiVRozsahu(String idOd, String idDo) {
        AbstrFIFO<Produkt> nalezene = tabulka.najdiVRozsahu(idOd, idDo);

        int kolik = 0;
        Iterator<Produkt> iterator = nalezene.vytvorIterator();
        while (iterator.hasNext()) {
            iterator.next();
            kolik++;
        }

        Produkt[] pole = new Produkt[kolik];
        for (int i = 0; i < kolik; i++) {
            pole[i] = nalezene.odeber();
        }
        return pole;
    }

    public void vybuduj() {
        if (pocet == 0) return;

        Produkt[] pole = new Produkt[pocet];
        Iterator<Produkt> iterator = tabulka.vytvorIterator(eTypProhl.HLOUBKA);
        int index = 0;
        while (iterator.hasNext()) {
            pole[index] = iterator.next();
            index++;
        }

        tabulka.zrus();

        AbstrFIFO<int[]> fronta = new AbstrFIFO<>();
        fronta.vloz(new int[]{0, pole.length - 1});

        while (!fronta.jePrazdny()) {
            int[] usek = fronta.odeber();
            int zacatek = usek[0];
            int konec = usek[1];
            if (zacatek > konec) continue;

            int stred = (zacatek + konec) / 2;
            tabulka.vloz(pole[stred].getId(), pole[stred]);

            fronta.vloz(new int[]{zacatek, stred - 1});
            fronta.vloz(new int[]{stred + 1, konec});
        }
    }

    public int vyskaStromu() {
        return tabulka.vyska();
    }

    public Iterator<Produkt> vytvorIterator(eTypProhl typ) {
        return tabulka.vytvorIterator(typ);
    }

    public void projdiStrom(AbstrTable.Navstevnik<String> navstevnik) {
        tabulka.projdiStrom(navstevnik);
    }

    public int generuj(int kolik) {
        enumSklad[] vsechnySklady = enumSklad.values();
        int vlozeno = 0;

        for (int i = 0; i < kolik; i++) {
            String id = najdiVolneId();
            if (id == null) break;

            String nazev = NAZVY[random.nextInt(NAZVY.length)];
            String kategorie = KATEGORIE[random.nextInt(KATEGORIE.length)];
            float cena = (random.nextInt(49500) + 500) / 100f;
            int mnozstvi = random.nextInt(200) + 1;
            enumSklad sklad = vsechnySklady[random.nextInt(vsechnySklady.length)];

            Produkt produkt = new Produkt(id, nazev, kategorie, cena, mnozstvi);
            if (vloz(produkt, enumPozice.POSLEDNI, sklad)) {
                vlozeno++;
            }
        }
        return vlozeno;
    }

    private String najdiVolneId() {
        int start = random.nextInt(MAX_CISLO_ID);

        for (int i = 0; i < MAX_CISLO_ID; i++) {
            int cislo = (start + i) % MAX_CISLO_ID + 1;
            String id = String.format("P%03d", cislo);
            if (tabulka.najdi(id) == null) return id;
        }
        return null;
    }

    public int importData(String soubor) {
        enumSklad[] vsechnySklady = enumSklad.values();
        int nacteno = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(soubor), StandardCharsets.UTF_8))) {
            String radek;
            boolean prvniRadek = true;

            while ((radek = br.readLine()) != null) {
                if (prvniRadek) {
                    prvniRadek = false;
                    if (radek.startsWith("﻿")) radek = radek.substring(1);
                }

                String[] casti = radek.split("[,;]");
                if (casti.length < 6) continue;

                try {
                    int cisloSkladu = Integer.parseInt(casti[0].trim());
                    if (cisloSkladu < 1 || cisloSkladu > vsechnySklady.length) continue;

                    String id = casti[1].trim();
                    String nazev = casti[2].trim();
                    String kategorie = casti[3].trim();
                    float cena = Float.parseFloat(casti[4].trim());
                    int mnozstvi = Integer.parseInt(casti[5].trim());

                    Produkt produkt = new Produkt(id, nazev, kategorie, cena, mnozstvi);
                    if (vloz(produkt, enumPozice.POSLEDNI, vsechnySklady[cisloSkladu - 1])) {
                        nacteno++;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            System.err.println("Chyba pri cteni souboru: " + e.getMessage());
        }
        return nacteno;
    }

    public int ulozData(String soubor) {
        int ulozeno = 0;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(soubor), StandardCharsets.UTF_8))) {
            bw.write("sklad;id;nazev;kategorie;cena;mnozstvi");
            bw.newLine();

            Iterator<Produkt> iterator = tabulka.vytvorIterator(eTypProhl.HLOUBKA);
            while (iterator.hasNext()) {
                Produkt p = iterator.next();
                bw.write((p.getSklad().ordinal() + 1) + ";" + p.getId() + ";" + p.getNazev() + ";"
                        + p.getKategorie() + ";" + p.getCena() + ";" + p.getMnozstvi());
                bw.newLine();
                ulozeno++;
            }
        } catch (IOException e) {
            System.err.println("Chyba pri zapisu souboru: " + e.getMessage());
            return -1;
        }
        return ulozeno;
    }

    public void zrus(enumSklad sklad) {
        if (sklad == null) {
            sklady.zrus(null);
            tabulka.zrus();
            pocet = 0;
            return;
        }

        for (Produkt produkt : sklady.zobrazProduktyList(sklad)) {
            tabulka.odeber(produkt.getId());
            pocet--;
        }
        sklady.zrus(sklad);
    }

    public int getPocet() {
        return pocet;
    }

    public List<Produkt> zobrazProduktyList(enumSklad sklad) {
        return sklady.zobrazProduktyList(sklad);
    }

    public List<Produkt> zobrazProduktyPodLimitem(enumSklad sklad, int limit) {
        return sklady.zobrazProduktyPodLimitem(sklad, limit);
    }

    public Produkt zpristupniProdukt(enumPozice pozice, enumSklad sklad) {
        return sklady.zpristupniProdukt(pozice, sklad);
    }

    public boolean nastavAktualni(String id) {
        Produkt produkt = tabulka.najdi(id);
        if (produkt == null) return false;
        return sklady.synchronizujAktualniPodleId(produkt.getSklad(), id);
    }
}
