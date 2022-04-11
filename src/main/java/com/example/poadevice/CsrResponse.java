package com.example.poadevice;

import java.util.List;
import java.io.Serializable;

public class CsrResponse implements Serializable {

    private long id;
    private List<String> certificateChain;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(List<String> certificateChain) {
        this.certificateChain = certificateChain;
    }
}
