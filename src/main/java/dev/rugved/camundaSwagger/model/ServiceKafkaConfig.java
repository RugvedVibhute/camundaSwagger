package dev.rugved.camundaSwagger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServiceKafkaConfig extends KafkaConfig {

    @JsonProperty("jolt-spec-ipne")
    private String joltSpecIpne;

    @JsonProperty("jolt-spec-npis")
    private String joltSpecNpis;

    public String getJoltSpecIpne() {
        return joltSpecIpne;
    }

    public void setJoltSpecIpne(String joltSpecIpne) {
        this.joltSpecIpne = joltSpecIpne;
    }

    public String getJoltSpecNpis() {
        return joltSpecNpis;
    }

    public void setJoltSpecNpis(String joltSpecNpis) {
        this.joltSpecNpis = joltSpecNpis;
    }
}