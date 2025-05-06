package dev.rugved.camundaSwagger.model;

import lombok.Data;

@Data
public class ConfigMapData {
    private Endpoints endpoints;

    public Endpoints getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoints endpoints) {
        this.endpoints = endpoints;
    }
}
