package com.example.poadevice.domain;

import java.io.Serializable;

public class ServiceDto implements Serializable {

    public String service;
    public String uri;

    public String getService() {
        return service;
    }

    public String getUri() {
        return uri;
    }

}
