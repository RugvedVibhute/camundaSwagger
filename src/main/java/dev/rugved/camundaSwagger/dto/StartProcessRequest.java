package dev.rugved.camundaSwagger.dto;


import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class StartProcessRequest {
    @NotNull(message = "bpmnProcessId is null")
    private String bpmnProcessId;

    @NotNull(message = "variables are null")
    private Map<String, Object> variables;

    // Getters and setters

    public String getBpmnProcessId() {
        return bpmnProcessId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
