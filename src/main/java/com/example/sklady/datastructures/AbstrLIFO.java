package com.example.sklady.datastructures;

import java.util.Iterator;

public class AbstrLIFO<T> implements IAbstrLIFO<T>{

    private final AbstrDoubleList<T> seznam = new AbstrDoubleList<>();

    @Override
    public void vloz(T data) {
        seznam.vlozPosledni(data);
    }

    @Override
    public T odeber() {
        return seznam.odeberPosledni();
    }

    @Override
    public void zrus() {
        seznam.zrus();
    }

    @Override
    public boolean jePrazdny() {
        return seznam.jePrazdny();
    }

    @Override
    public Iterator<T> vytvorIterator() {
        return seznam.iterator();
    }
}
