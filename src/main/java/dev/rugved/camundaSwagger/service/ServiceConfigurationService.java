package dev.rugved.camundaSwagger.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.rugved.camundaSwagger.config.ServiceConfigMapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServiceConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceConfigurationService.class);

    private final String configMapPath;
    private final ObjectMapper yamlMapper;

    @Autowired
    public ServiceConfigurationService(ServiceConfigMapConfig config) {
        this.configMapPath = config.getConfigMapPath();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Reads service configuration from the ConfigMap file.
     * This directly reads the YAML as a Map to handle any structure differences.
     *
     * @return Map containing the configuration, or null if an error occurs
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getServiceConfiguration() {
        logger.debug("Reading service configuration from ConfigMap at path: {}", configMapPath);

        try {
            File configFile = new File(configMapPath);
            if (!configFile.exists()) {
                logger.error("Service ConfigMap file does not exist: {}", configMapPath);
                return null;
            }

            // Read the YAML file as a generic Map
            Map<String, Object> configMap = yamlMapper.readValue(configFile, HashMap.class);

            // Navigate to endpoints.kafka
            if (configMap.containsKey("endpoints")) {
                Map<String, Object> endpoints = (Map<String, Object>) configMap.get("endpoints");
                if (endpoints.containsKey("kafka")) {
                    return (Map<String, Object>) endpoints.get("kafka");
                } else {
                    logger.error("Kafka configuration not found in endpoints");
                    return null;
                }
            } else {
                logger.error("Endpoints not found in configuration file");
                return null;
            }

        } catch (IOException e) {
            logger.error("Failed to read service configuration from ConfigMap: {}", e.getMessage(), e);
            return null;
        }
    }
}