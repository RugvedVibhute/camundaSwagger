package dev.rugved.camundaSwagger.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InstallationMethod {

    private static final Logger logger = LoggerFactory.getLogger(InstallationMethod.class);

    @JobWorker(type = "InstallationMethod")
    public void installationMethod(final JobClient client, final ActivatedJob job) {
        try {
            // Fetch and parse variables
            String var = job.getVariables();
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse the JSON string into a Map
            Map<String, Object> variablesMap = objectMapper.readValue(var, Map.class);

            // Extract the "shippingOrderCharacteristic" from the top level
            List<Map<String, Object>> shippingOrderCharacteristic =
                    (List<Map<String, Object>>) variablesMap.get("shippingOrderCharacteristic");

            // Find "InstallationMethod"
            String installationMethod = null;
            if (shippingOrderCharacteristic != null) {
                for (Map<String, Object> characteristic : shippingOrderCharacteristic) {
                    if ("InstallationMethod".equals(characteristic.get("name"))) {
                        installationMethod = (String) characteristic.get("value");
                        break;
                    }
                }
            }

            // Prepare the output map
            Map<String, Object> output = new HashMap<>();
            if (installationMethod != null) {
                output.put("InstallationMethod", installationMethod);
            } else {
                logger.warn("InstallationMethod not found in shippingOrderCharacteristic");
            }

            // Complete the job and send variables back to Zeebe
            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            logger.info("Job completed with variables: {}", output);

        } catch (Exception e) {
            logger.error("Error processing InstallationMethod job", e);
        }
    }
}
