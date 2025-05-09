package dev.rugved.camundaSwagger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.service")
public class ServiceConfigMapConfig {

    // Default path for the service config
    private String configMapPath = "/app/config/endpoints-config-service.yaml";

    public String getConfigMapPath() {
        return configMapPath;
    }

    public void setConfigMapPath(String configMapPath) {
        this.configMapPath = configMapPath;
    }
}