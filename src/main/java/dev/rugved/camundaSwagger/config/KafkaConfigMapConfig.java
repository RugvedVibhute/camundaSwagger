package dev.rugved.camundaSwagger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaConfigMapConfig {

    // Default path matches the one imported in application.yaml
    private String configMapPath = "/app/config/endpoints-config-shipment.yaml";

    public String getConfigMapPath() {
        return configMapPath;
    }

    public void setConfigMapPath(String configMapPath) {
        this.configMapPath = configMapPath;
    }
}