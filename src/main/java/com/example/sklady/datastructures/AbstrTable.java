package com.example.sklady.datastructures;

import com.example.sklady.enums.eTypProhl;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class AbstrTable<K extends Comparable<K>, V> implements IAbstrTable<K, V> {
    private static class Node<K, V> {

        K key; // ключ Р1243
        V value; // посилання на обєкт
        Node<K, V> left; // ну і посилання на інші обєкти
        Node<K, V> right;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    private Node<K, V> root;

    @Override
    public void zrus() {
        root = null;
    }

    @Override
    public boolean jePrazdny() {
        return root == null;
    }

    @Override
    public V najdi(K key) {
        if (key == null) return null;

        Node<K, V> aktualni = root;

        while (aktualni != null) {
            int c = key.compareTo(aktualni.key);

            if (c == 0) {
                return aktualni.value;
            } else if (c < 0) {
                aktualni = aktualni.left;
            } else {
                aktualni = aktualni.right;
            }
        }
        return null;
    }

    @Override
    public void vloz(K key, V value) {
        if (key == null) return;

        Node<K, V> novy = new Node<>(key, value);

        if (root == null) {
            root = novy;
            return;
        }

        Node<K, V> aktualni = root;

        while (aktualni != null) {
            int c = key.compareTo(aktualni.key);

            if (c == 0) {
                return;
            } else if (c < 0) {
                if (aktualni.left == null) {
                    aktualni.left = novy;
                    return;
                }
                aktualni = aktualni.left;
            } else {
                if (aktualni.right == null) {
                    aktualni.right = novy;
                    return;
                }
                aktualni = aktualni.right;
            }
        }
    }

    @Override
    public V odeber(K key) {
        if (key == null) return null;

        Node<K, V> rodic = null;
        Node<K, V> aktualni = root;

        while (aktualni != null) {
            int c = key.compareTo(aktualni.key);

            if (c == 0) break;

            rodic = aktualni;
            if (c < 0) aktualni = aktualni.left;
            else aktualni = aktualni.right;
        }

        if (aktualni == null) return null;

        V hodnota = aktualni.value;

        if (aktualni.left != null && aktualni.right != null) {
            Node<K, V> rodicNaslednika = aktualni;
            Node<K, V> naslednik = aktualni.right;

            while (naslednik.left != null) {
                rodicNaslednika = naslednik;
                naslednik = naslednik.left;
            }

            aktualni.key = naslednik.key;
            aktualni.value = naslednik.value;

            rodic = rodicNaslednika;
            aktualni = naslednik;
        }

        Node<K, V> dite;
        if (aktualni.left != null) dite = aktualni.left;
        else dite = aktualni.right;

        if (rodic == null) {
            root = dite;
        } else if (rodic.left == aktualni) {
            rodic.left = dite;
        } else {
            rodic.right = dite;
        }

        return hodnota;
    }

    public AbstrFIFO<V> najdiVRozsahu(K klicOd, K klicDo) {
        AbstrFIFO<V> vysledek = new AbstrFIFO<>();
        if (klicOd == null || klicDo == null || klicOd.compareTo(klicDo) > 0) return vysledek;

        AbstrLIFO<Node<K, V>> zasobnik = new AbstrLIFO<>();
        Node<K, V> aktualni = root;

        while (aktualni != null || !zasobnik.jePrazdny()) {
            if (aktualni != null) {
                if (klicOd.compareTo(aktualni.key) <= 0) {
                    zasobnik.vloz(aktualni);
                    aktualni = aktualni.left;
                } else {
                    aktualni = aktualni.right;
                }
            } else {
                Node<K, V> uzel = zasobnik.odeber();
                if (uzel.key.compareTo(klicDo) > 0) break;

                vysledek.vloz(uzel.value);
                aktualni = uzel.right;
            }
        }
        return vysledek;
    }

    public int vyska() {
        if (root == null) return 0;

        AbstrFIFO<Node<K, V>> fronta = new AbstrFIFO<>();
        fronta.vloz(root);
        int vyska = 0;
        int uzluVUrovni = 1;

        while (uzluVUrovni > 0) {
            int uzluVDalsiUrovni = 0;

            for (int i = 0; i < uzluVUrovni; i++) {
                Node<K, V> uzel = fronta.odeber();
                if (uzel.left != null) {
                    fronta.vloz(uzel.left);
                    uzluVDalsiUrovni++;
                }
                if (uzel.right != null) {
                    fronta.vloz(uzel.right);
                    uzluVDalsiUrovni++;
                }
            }

            vyska++;
            uzluVUrovni = uzluVDalsiUrovni;
        }
        return vyska;
    }

    public interface Navstevnik<K> {
        void navstiv(K klic, K levy, K pravy, int hloubka);
    }

    public void projdiStrom(Navstevnik<K> navstevnik) {
        if (root == null) return;

        AbstrFIFO<Node<K, V>> fronta = new AbstrFIFO<>();
        AbstrFIFO<Integer> hloubky = new AbstrFIFO<>();
        fronta.vloz(root);
        hloubky.vloz(0);

        while (!fronta.jePrazdny()) {
            Node<K, V> uzel = fronta.odeber();
            int hloubka = hloubky.odeber();

            K levy = uzel.left == null ? null : uzel.left.key;
            K pravy = uzel.right == null ? null : uzel.right.key;
            navstevnik.navstiv(uzel.key, levy, pravy, hloubka);

            if (uzel.left != null) {
                fronta.vloz(uzel.left);
                hloubky.vloz(hloubka + 1);
            }
            if (uzel.right != null) {
                fronta.vloz(uzel.right);
                hloubky.vloz(hloubka + 1);
            }
        }
    }

    @Override
    public Iterator<V> vytvorIterator(eTypProhl typ) {
        if (typ == eTypProhl.SIRKA) return new IteratorSirka();
        return new IteratorHloubka();
    }

    private class IteratorSirka implements Iterator<V> {

        private final AbstrFIFO<Node<K, V>> fronta = new AbstrFIFO<>();

        IteratorSirka() {
            if (root != null) fronta.vloz(root);
        }

        @Override
        public boolean hasNext() {
            return !fronta.jePrazdny();
        }

        @Override
        public V next() {
            if (!hasNext()) throw new NoSuchElementException();

            Node<K, V> uzel = fronta.odeber();
            if (uzel.left != null) fronta.vloz(uzel.left);
            if (uzel.right != null) fronta.vloz(uzel.right);
            return uzel.value;
        }
    }

    private class IteratorHloubka implements Iterator<V> {

        private final AbstrLIFO<Node<K, V>> zasobnik = new AbstrLIFO<>();

        IteratorHloubka() {
            vlozLevouVetev(root);
        }

        private void vlozLevouVetev(Node<K, V> uzel) {
            while (uzel != null) {
                zasobnik.vloz(uzel);
                uzel = uzel.left;
            }
        }

        @Override
        public boolean hasNext() {
            return !zasobnik.jePrazdny();
        }

        @Override
        public V next() {
            if (!hasNext()) throw new NoSuchElementException();

            Node<K, V> uzel = zasobnik.odeber();
            vlozLevouVetev(uzel.right);
            return uzel.value;
        }
    }

}