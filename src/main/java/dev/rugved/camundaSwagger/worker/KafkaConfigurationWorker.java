package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.model.KafkaConfig;
import dev.rugved.camundaSwagger.service.ErrorHandlerService;
import dev.rugved.camundaSwagger.service.KafkaConfigurationService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static dev.rugved.camundaSwagger.util.Constants.*;

/**
 * Worker responsible for fetching Kafka configuration from ConfigMap
 * and providing it to Camunda process instances.
 */
@Component
public class KafkaConfigurationWorker {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfigurationWorker.class);
    private static final String JOB_TYPE_KAFKA_CONFIG = "kafka-config-worker";

    private final KafkaConfigurationService kafkaConfigService;
    private final ErrorHandlerService errorHandlerService;

    @Autowired
    public KafkaConfigurationWorker(
            KafkaConfigurationService kafkaConfigService,
            ErrorHandlerService errorHandlerService) {
        this.kafkaConfigService = kafkaConfigService;
        this.errorHandlerService = errorHandlerService;
    }

    @JobWorker(type = JOB_TYPE_KAFKA_CONFIG)
    public void fetchKafkaConfig(final JobClient client, final ActivatedJob job) {
        Map<String, Object> output = new HashMap<>();

        try {
            logger.info("Processing Kafka configuration job - jobKey: {}", job.getKey());

            // Get Kafka configuration from ConfigMap
            KafkaConfig kafkaConfig = kafkaConfigService.getKafkaConfiguration();

            if (kafkaConfig == null) {
                throw new IllegalArgumentException("Failed to retrieve Kafka configuration from ConfigMap");
            }

            // Add the three Kafka configuration values to output
            output.put("kafkaBootstrapServer", kafkaConfig.getBootstrapServer());
            output.put("kafkaTopic", kafkaConfig.getTopic());
            output.put("kafkaAdditionalProperties", kafkaConfig.getAdditionalProperties());

            // Clean error fields
            output.put(ERROR_MESSAGE, null);
            output.put(ERROR_CODE, null);
            output.put(ERROR_DETAILS, null);

            logger.info("Successfully retrieved Kafka configuration - jobKey: {}, bootstrap server found: {}, topic found: {}",
                    job.getKey(),
                    kafkaConfig.getBootstrapServer() != null,
                    kafkaConfig.getTopic() != null);

            // Complete the job with Kafka configuration variables
            client.newCompleteCommand(job.getKey())
                    .variables(output)
                    .send()
                    .join();

            logger.info("Kafka configuration job completed successfully - jobKey: {}", job.getKey());

        } catch (Exception e) {
            logger.error("Error processing {} job: {}", JOB_TYPE_KAFKA_CONFIG, e.getMessage());

            // Handle error and set error codes instead of failing
            output = errorHandlerService.handleError(e, JOB_TYPE_KAFKA_CONFIG);

            logger.error("Kafka configuration job completed with error - jobKey: {}, errorCode: {}",
                    job.getKey(), output.containsKey(ERROR_CODE) ? output.get(ERROR_CODE) : "UNKNOWN");

            client.newCompleteCommand(job.getKey())
                    .variables(output)
                    .send()
                    .join();
        }
    }
}