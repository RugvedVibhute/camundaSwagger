package dev.rugved.camundaSwagger.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.rugved.camundaSwagger.config.KafkaConfigMapConfig;
import dev.rugved.camundaSwagger.model.ConfigMapData;
import dev.rugved.camundaSwagger.model.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class KafkaConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfigurationService.class);

    private final String configMapPath;
    private final ObjectMapper yamlMapper;

    @Autowired
    public KafkaConfigurationService(KafkaConfigMapConfig config) {
        this.configMapPath = config.getConfigMapPath();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Reads Kafka configuration from the ConfigMap file.
     *
     * @return KafkaConfig object containing the configuration, or null if an error occurs
     */
    public KafkaConfig getKafkaConfiguration() {
        logger.debug("Reading Kafka configuration from ConfigMap at path: {}", configMapPath);

        try {
            File configFile = new File(configMapPath);
            if (!configFile.exists()) {
                logger.error("ConfigMap file does not exist: {}", configMapPath);
                return null;
            }

            ConfigMapData configMapData = yamlMapper.readValue(configFile, ConfigMapData.class);
            return configMapData.getEndpoints().getKafka();

        } catch (IOException e) {
            logger.error("Failed to read Kafka configuration from ConfigMap: {}", e.getMessage(), e);
            return null;
        }
    }
}