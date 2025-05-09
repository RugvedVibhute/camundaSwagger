package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.service.ErrorHandlerService;
import dev.rugved.camundaSwagger.service.ServiceConfigurationService;
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
 * Worker responsible for fetching service configuration from ConfigMap
 * and providing it to Camunda process instances.
 */
@Component
public class ServiceConfigurationWorker {

    private static final Logger logger = LoggerFactory.getLogger(ServiceConfigurationWorker.class);
    private static final String JOB_TYPE_SERVICE_CONFIG = "service-config-worker";

    private final ServiceConfigurationService serviceConfigService;
    private final ErrorHandlerService errorHandlerService;

    @Autowired
    public ServiceConfigurationWorker(
            ServiceConfigurationService serviceConfigService,
            ErrorHandlerService errorHandlerService) {
        this.serviceConfigService = serviceConfigService;
        this.errorHandlerService = errorHandlerService;
    }

    @SuppressWarnings("unchecked")
    @JobWorker(type = JOB_TYPE_SERVICE_CONFIG)
    public void fetchServiceConfig(final JobClient client, final ActivatedJob job) {
        Map<String, Object> output = new HashMap<>();

        try {
            logger.info("Processing service configuration job - jobKey: {}", job.getKey());

            // Get Service configuration from ConfigMap
            Map<String, Object> serviceConfig = serviceConfigService.getServiceConfiguration();

            if (serviceConfig == null) {
                throw new IllegalArgumentException("Failed to retrieve service configuration from ConfigMap");
            }

            // Extract Kafka configuration properties
            output.put("serviceKafkaBootstrapServer", serviceConfig.get("bootstrap-server"));
            output.put("serviceKafkaTopic", serviceConfig.get("topic"));
            output.put("serviceKafkaAdditionalProperties", serviceConfig.get("additional-properties"));

            // Add Jolt specifications if present
            if (serviceConfig.containsKey("jolt-spec-ipne")) {
                output.put("joltSpecIpne", serviceConfig.get("jolt-spec-ipne"));
            }

            if (serviceConfig.containsKey("jolt-spec-npis")) {
                output.put("joltSpecNpis", serviceConfig.get("jolt-spec-npis"));
            }

            // Clean error fields
            output.put(ERROR_MESSAGE, null);
            output.put(ERROR_CODE, null);
            output.put(ERROR_DETAILS, null);

            logger.info("Successfully retrieved service configuration - jobKey: {}, bootstrap server found: {}, topic found: {}, jolt specs found: {}",
                    job.getKey(),
                    serviceConfig.containsKey("bootstrap-server"),
                    serviceConfig.containsKey("topic"),
                    serviceConfig.containsKey("jolt-spec-ipne") && serviceConfig.containsKey("jolt-spec-npis"));

            // Complete the job with service configuration variables
            client.newCompleteCommand(job.getKey())
                    .variables(output)
                    .send()
                    .join();

            logger.info("Service configuration job completed successfully - jobKey: {}", job.getKey());

        } catch (Exception e) {
            logger.error("Error processing {} job: {}", JOB_TYPE_SERVICE_CONFIG, e.getMessage());

            // Handle error and set error codes instead of failing
            output = errorHandlerService.handleError(e, JOB_TYPE_SERVICE_CONFIG);

            logger.error("Service configuration job completed with error - jobKey: {}, errorCode: {}",
                    job.getKey(), output.containsKey(ERROR_CODE) ? output.get(ERROR_CODE) : "UNKNOWN");

            client.newCompleteCommand(job.getKey())
                    .variables(output)
                    .send()
                    .join();
        }
    }
}