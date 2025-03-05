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

import static dev.rugved.camundaSwagger.util.Constants.*;

@Component
public class FetchVariablesWorker {

    private static final Logger logger = LoggerFactory.getLogger(FetchVariablesWorker.class);

    @JobWorker(type = JOB_TYPE_FETCHVARIABLES)
    public void fetchDbQueryParams(final JobClient client, final ActivatedJob job) {
        try {
            // Retrieve job variables as a JSON string
            String var = job.getVariables();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(var);
            // Initialize variables to store extracted values
            String networkElement = null, distance = null, ntuRequired = null, ntuSize = null;
            String uniPortCapacity = null, uniInterfaceType = null, stateOrProvince = null;
            String installationMethod = null, CorrelationId = null;

            // Extract "stateOrProvince"
            JsonNode stateNode = rootNode.path(RELATED_PARTY).get(1).path(CONTACT_MEDIUM).get(0).path(CHARACTERISTIC).path(STATE_OR_PROVINCE);
            if (!stateNode.isMissingNode()) {
                stateOrProvince = stateNode.asText();
            }
            // Extract shipping order characteristics
            JsonNode shippingOrderItems = rootNode.path(SHIPPING_ORDER_ITEM);
            if (shippingOrderItems.isArray()) {
                for (JsonNode item : shippingOrderItems) {
                    JsonNode shipmentItems = item.path(SHIPMENT).path(SHIPMENT_ITEM);
                    if (shipmentItems.isArray()) {
                        for (JsonNode shipmentItem : shipmentItems) {
                            // Navigate to product characteristics
                            JsonNode product = shipmentItem.path("product");
                            JsonNode productCharacteristics = product.path("productSpecification");
                            // Extract relevant parameters based on characteristic names
                            if (productCharacteristics.path("id").asText().equals("zz123")) {
                                for (JsonNode characteristic : productCharacteristics) {
                                    String name = characteristic.path("name").asText();
                                    String value = characteristic.path("value").asText();
                                    switch (name) {
                                        case NETWORK_ELEMENT:
                                            networkElement = value;
                                            break;
                                        case DISTANCE:
                                            distance = value;
                                            break;
                                        case NTU_REQUIRED_REQUEST:
                                            ntuRequired = value;
                                            break;
                                        case NTU_SIZE:
                                            ntuSize = value;
                                            break;
                                        case UNI_PORT_CAPACITY_REQUEST:
                                            uniPortCapacity = value;
                                            break;
                                        case INTERFACE_TYPE:
                                            uniInterfaceType = value;
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Extract "InstallationMethod" from "shippingOrderCharacteristic"
            JsonNode shippingOrderCharacteristic = rootNode.path(SHIPPING_ORDER_CHARACTERISTIC);
            if (shippingOrderCharacteristic.isArray()) {
                for (JsonNode characteristic : shippingOrderCharacteristic) {
                    String name = characteristic.path("name").asText();
                    String value = characteristic.path("value").asText();
                    switch (name) {
                        case INSTALLATION_METHOD:
                            installationMethod = value;
                            break;
                        case "CorrelationId":
                            CorrelationId = value;
                            break;
                    }
                }
            }

            // Apply NTURequired logic: If NTURequired is "No", set ntuSize to "0"
            if ("No".equalsIgnoreCase(ntuRequired)) {
                ntuSize = "0";
            }

            // Prepare output variables to send back
            Map<String, Object> output = new HashMap<>();
            output.put(NETWORK_ELEMENT, networkElement);
            output.put(DISTANCE, distance);
            output.put(NTU_REQUIRED, ntuRequired);
            output.put(NTU_SIZE, ntuSize);
            output.put(UNI_PORT_CAPACITY, uniPortCapacity);
            output.put(UNI_INTERFACE_TYPE, uniInterfaceType);
            output.put(STATE_OR_PROVINCE, stateOrProvince);
            output.put(INSTALLATION_METHOD, installationMethod);
            output.put("CorrelationId", CorrelationId);

            // Complete the job with extracted variables
            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            logger.info("Job completed with extracted parameters: {}", output);

        } catch (Exception e) {
            // Log and handle exceptions by sending an error message
            logger.error("Error processing fetchDbQueryParams job", e);
            Map<String, Object> errorOutput = Map.of(ERROR_MESSAGE, e.getMessage(), "errorCode", "CAM-" + job.getKey());

            client.newCompleteCommand(job.getKey()).variables(errorOutput).send().join();
        }
    }
}
