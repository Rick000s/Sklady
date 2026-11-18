package com.example.sklady.datastructures;

import com.example.sklady.enums.eTypProhl;

import java.util.Iterator;

public interface IAbstrTable<K extends Comparable<K>, V> {
    void zrus();
    boolean jePrazdny();
    V najdi(K key);
    void vloz(K key, V value);
    V odeber(K key);
    Iterator<V> vytvorIterator(eTypProhl typ);
}