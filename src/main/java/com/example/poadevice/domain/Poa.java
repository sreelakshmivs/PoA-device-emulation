package com.example.poadevice.domain;

public class Poa {

    private final int id;
    private final String poa;

    public Poa(final int id, final String poa) {
        this.id = id;
        this.poa = poa;
    }

    public int getId() {
        return id;
    }

    public String getPoa() {
        return poa;
    }

}
