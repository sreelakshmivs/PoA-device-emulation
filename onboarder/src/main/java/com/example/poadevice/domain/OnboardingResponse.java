package com.example.poadevice.domain;

import java.util.List;
import java.io.Serializable;

public class OnboardingResponse implements Serializable {

    private List<String> certificateChain;
    public ServiceDto deviceRegistry;
    public ServiceDto systemRegistry;
    public ServiceDto serviceRegistry;
    public ServiceDto orchestrationService;

    public List<String> getCertificateChain() {
        return certificateChain;
    }

    public ServiceDto getDeviceRegistry() {
        return this.deviceRegistry;
    }

    public ServiceDto getSystemRegistry() {
        return this.systemRegistry;
    }

    public ServiceDto getServiceRegistry() {
        return this.serviceRegistry;
    }

    public ServiceDto getOrchestrationService() {
        return this.orchestrationService;
    }

}
