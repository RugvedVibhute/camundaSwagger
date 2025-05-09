package dev.rugved.camundaSwagger.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.rugved.camundaSwagger.config.ServiceConfigMapConfig;
import dev.rugved.camundaSwagger.model.ConfigMapData;
import dev.rugved.camundaSwagger.model.KafkaConfig;
import dev.rugved.camundaSwagger.model.ServiceKafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

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
     *
     * @return ServiceKafkaConfig object containing the configuration, or null if an error occurs
     */
    public ServiceKafkaConfig getServiceConfiguration() {
        logger.debug("Reading service configuration from ConfigMap at path: {}", configMapPath);

        try {
            File configFile = new File(configMapPath);
            if (!configFile.exists()) {
                logger.error("Service ConfigMap file does not exist: {}", configMapPath);
                return null;
            }

            ConfigMapData configMapData = yamlMapper.readValue(configFile, ConfigMapData.class);

            // Cast to ServiceKafkaConfig to access Jolt specifications
            if (configMapData.getEndpoints().getKafka() instanceof ServiceKafkaConfig) {
                return (ServiceKafkaConfig) configMapData.getEndpoints().getKafka();
            } else {
                // Handle regular KafkaConfig
                KafkaConfig kafkaConfig = configMapData.getEndpoints().getKafka();
                ServiceKafkaConfig serviceConfig = new ServiceKafkaConfig();
                serviceConfig.setBootstrapServer(kafkaConfig.getBootstrapServer());
                serviceConfig.setTopic(kafkaConfig.getTopic());
                serviceConfig.setAdditionalProperties(kafkaConfig.getAdditionalProperties());
                return serviceConfig;
            }

        } catch (IOException e) {
            logger.error("Failed to read service configuration from ConfigMap: {}", e.getMessage(), e);
            return null;
        }
    }
}