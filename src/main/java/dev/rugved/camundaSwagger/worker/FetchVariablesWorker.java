package dev.rugved.camundaSwagger.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FetchVariablesWorker {

    private static final Logger logger = LoggerFactory.getLogger(FetchVariablesWorker.class);

    @JobWorker(type = "fetchVariables")
    public void fetchDbQueryParams(final JobClient client, final ActivatedJob job) {
        try {
            // Retrieve job variables as a JSON string
            String var = job.getVariables();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(var);

            // Initialize variables to store extracted values
            String networkElement = null, distance = null, ntuRequired = null, ntuSize = null;
            String uniPortCapacity = null, uniInterfaceType = null, stateOrProvince = null;

            // Extract "stateOrProvince" from JSON path: relatedParty -> contactMedium -> characteristic
            JsonNode stateNode = rootNode.path("relatedParty").get(1)
                    .path("contactMedium").get(0)
                    .path("characteristic")
                    .path("stateOrProvince");
            if (!stateNode.isMissingNode()) {
                stateOrProvince = stateNode.asText();
            }

            // Extract shipping order characteristics
            JsonNode shippingOrderItems = rootNode.path("shippingOrderItem");
            if (shippingOrderItems.isArray()) {
                for (JsonNode item : shippingOrderItems) {
                    JsonNode shipmentItems = item.path("shipment").path("shipmentItem");
                    if (shipmentItems.isArray()) {
                        for (JsonNode shipmentItem : shipmentItems) {
                            // Navigate to product characteristics
                            JsonNode productCharacteristics = shipmentItem
                                    .path("product")
                                    .path("productCharacteristic");

                            // Extract relevant parameters based on characteristic names
                            for (JsonNode characteristic : productCharacteristics) {
                                String name = characteristic.path("name").asText();
                                String value = characteristic.path("value").asText();
                                switch (name) {
                                    case "networkElement": networkElement = value; break;
                                    case "distance": distance = value; break;
                                    case "NTURequired": ntuRequired = value; break;
                                    case "ntuSize": ntuSize = value; break;
                                    case "UNIPortCapacity": uniPortCapacity = value; break;
                                    case "InterfaceType": uniInterfaceType = value; break;
                                }
                            }
                        }
                    }
                }
            }

            // Apply NTURequired logic: If NTURequired is "No", set ntuSize to "0"
            if ("No".equalsIgnoreCase(ntuRequired)) {
                ntuSize = "0";
            }

            // Prepare output variables to send back
            Map<String, Object> output = new HashMap<>();
            output.put("networkElement", networkElement);
            output.put("distance", distance);
            output.put("ntuRequired", ntuRequired);
            output.put("ntuSize", ntuSize);
            output.put("uniPortCapacity", uniPortCapacity);
            output.put("uniInterfaceType", uniInterfaceType);
            output.put("stateOrProvince", stateOrProvince);

            // Complete the job with extracted variables
            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            logger.info("Job completed with extracted parameters: {}", output);

        } catch (Exception e) {
            // Log and handle exceptions by sending an error message
            logger.error("Error processing fetchDbQueryParams job", e);
            Map<String, Object> output = new HashMap<>();
            output.put("errorMessage", " " + e.getMessage());

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        }
    }
}
