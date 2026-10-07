package com.example.sklady.managers;

import com.example.sklady.datastructures.AbstractDoubleList;
import com.example.sklady.enums.enumPozice;
import com.example.sklady.enums.enumSklad;
import com.example.sklady.models.Produkt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Sklady {

    private final AbstractDoubleList<Produkt>[] sklady;

    public Sklady() {
        int pocetSkladu = enumSklad.values().length;
        sklady = new AbstractDoubleList[pocetSkladu];
        for (int i = 0; i < pocetSkladu; i++) {
            sklady[i] = new AbstractDoubleList<>();
        }
    }

    public int importData(String soubor) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(soubor))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (firstLine) {
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1);
                    }
                    if (line.toLowerCase().contains("sklad") || line.toLowerCase().contains("id")) {
                        firstLine = false;
                        continue;
                    }
                    firstLine = false;
                }

                String[] parts = line.split("[,;]");
                if (parts.length >= 6) {
                    int skladIndex = Integer.parseInt(parts[0].trim()) - 1; // Число від 1 до N відповідає index + 1[cite: 4]
                    String id = parts[1].trim();
                    String nazev = parts[2].trim();
                    String kategorie = parts[3].trim();
                    float cena = Float.parseFloat(parts[4].trim());
                    int mnozstvi = Integer.parseInt(parts[5].trim());

                    Produkt produkt = new Produkt(id, nazev, kategorie, cena, mnozstvi);

                    if (skladIndex >= 0 && skladIndex < sklady.length) {
                        sklady[skladIndex].vlozPosledni(produkt);
                        count++;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка читання файлу: " + e.getMessage());
        }
        return count;
    }

    public void vlozProdukt(Produkt produkt, enumPozice pozice, enumSklad sklad) {
        if (produkt == null || sklad == null || pozice == null) return;
        int index = sklad.ordinal();
        if (index < 0 || index >= sklady.length) return;

        AbstractDoubleList<Produkt> list = sklady[index];
        switch (pozice) {
            case PRVNI:
                list.vlozPrvni(produkt);
                break;
            case POSLEDNI:
                list.vlozPosledni(produkt);
                break;
            case PREDCHUDCE:
                list.vlozPredchudce(produkt);
                break;
            case NASLEDNIK:
                list.vlozNaslednika(produkt);
                break;
            default:
                list.vlozPosledni(produkt);
                break;
        }
    }

    public Produkt zpristupniProdukt(enumPozice pozice, enumSklad sklad) {
        if (sklad == null || pozice == null) return null;
        int index = sklad.ordinal();
        if (index < 0 || index >= sklady.length) return null;

        AbstractDoubleList<Produkt> list = sklady[index];
        switch (pozice) {
            case PRVNI:
                return list.zpristupniPrvni();
            case POSLEDNI:
                return list.zpristupniPosledni();
            case PREDCHUDCE:
                return list.zpristupniPredchudce();
            case NASLEDNIK:
                return list.zpristupniNaslednika();
            case AKTUALNI:
                return list.zpristupniAktualni();
            default:
                return null;
        }
    }

    public boolean odeberProduktPodleId(enumSklad sklad, String id) {
        if (sklad == null || id == null) return false;
        int index = sklad.ordinal();
        if (index < 0 || index >= sklady.length) return false;

        AbstractDoubleList<Produkt> list = sklady[index];
        for (Produkt p : list) {
            if (p.getId().equals(id)) {
                list.odeberAktualni();
                return true;
            }
        }
        return false;
    }

    public Produkt odeberProdukt(enumPozice pozice, enumSklad sklad) {
        if (sklad == null || pozice == null) return null;
        int index = sklad.ordinal();
        if (index < 0 || index >= sklady.length) return null;

        AbstractDoubleList<Produkt> list = sklady[index];
        switch (pozice) {
            case PRVNI:
                return list.odeberPrvni();
            case POSLEDNI:
                return list.odeberPosledni();
            case PREDCHUDCE:
                return list.odeberPredchudce();
            case NASLEDNIK:
                return list.odeberNaslednika();
            case AKTUALNI:
                return list.odeberAktualni();
            default:
                return null;
        }
    }

    public void synchronizujAktualniPodleId(enumSklad sklad, String id) {
        if (sklad == null || id == null) return;
        int index = sklad.ordinal();
        if (index < 0 || index >= sklady.length) return;

        AbstractDoubleList<Produkt> list = sklady[index];
        for (Produkt p : list) {
            if (p.getId().equals(id)) {
                break;
            }
        }
    }

    public void presunProdukt(enumSklad zdroj, enumSklad cil) {
        if (zdroj == null || cil == null || zdroj == cil) return;
        int zdrojIndex = zdroj.ordinal();
        int cilIndex = cil.ordinal();

        if (zdrojIndex < 0 || zdrojIndex >= sklady.length || cilIndex < 0 || cilIndex >= sklady.length) return;

        AbstractDoubleList<Produkt> sourceList = sklady[zdrojIndex];
        AbstractDoubleList<Produkt> targetList = sklady[cilIndex];

        Produkt currentProduct = sourceList.zpristupniAktualni();
        if (currentProduct != null) {
            sourceList.odeberAktualni();
            targetList.vlozPosledni(currentProduct);
        }
    }

    public float zjistiPrumernouCenu(enumSklad sklad) {
        List<Produkt> produkty = zobrazProduktyList(sklad);
        if (produkty.isEmpty()) {
            return 0.0f;
        }
        float totalCena = 0;
        for (Produkt p : produkty) {
            totalCena += p.getCena();
        }
        return totalCena / produkty.size();
    }

    public List<Produkt> zobrazProduktyList(enumSklad sklad) {
        List<Produkt> result = new ArrayList<>();
        if (sklad == null) {
            for (int i = 0; i < sklady.length; i++) {
                for (Produkt p : sklady[i]) {
                    result.add(p);
                }
            }
        } else {
            int index = sklad.ordinal();
            if (index >= 0 && index < sklady.length) {
                for (Produkt p : sklady[index]) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    public List<Produkt> zobrazProduktyPodLimitem(enumSklad sklad, int limit) {
        List<Produkt> result = new ArrayList<>();
        List<Produkt> baseList = zobrazProduktyList(sklad);
        for (Produkt p : baseList) {
            if (p.getMnozstvi() < limit) {
                result.add(p);
            }
        }
        return result;
    }

    public void zrus(enumSklad sklad) {
        if (sklad == null) {
            for (int i = 0; i < sklady.length; i++) {
                sklady[i].zrus();
            }
        } else {
            int index = sklad.ordinal();
            if (index >= 0 && index < sklady.length) {
                sklady[index].zrus();
            }
        }
    }
}