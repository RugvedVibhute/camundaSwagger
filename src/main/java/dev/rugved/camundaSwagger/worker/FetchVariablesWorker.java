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
            String installationMethod = null;

            // Extract "stateOrProvince"
            JsonNode relatedParties = rootNode.path(RELATED_PARTY);
            if (relatedParties.isArray()) {
                for (JsonNode party : relatedParties) {
                    JsonNode contactMediums = party.path(CONTACT_MEDIUM);
                    if (contactMediums.isArray()) {
                        for (JsonNode contactMedium : contactMediums) {
                            JsonNode characteristicNode = contactMedium.path(CHARACTERISTIC);
                            if (characteristicNode.has(STATE_OR_PROVINCE)) {
                                stateOrProvince = characteristicNode.path(STATE_OR_PROVINCE).asText();
                                break; // Stop searching once found
                            }
                        }
                    }
                }
            }

            // Extract shipping order characteristics
            JsonNode shippingOrderItems = rootNode.path(SHIPPING_ORDER_ITEM);
            if (shippingOrderItems.isArray()) {
                for (JsonNode item : shippingOrderItems) {
                    JsonNode shipmentItems = item.path(SHIPMENT).path(SHIPMENT_ITEM);
                    if (shipmentItems.isArray()) {
                        for (JsonNode shipmentItem : shipmentItems) {
                            // Get product specification name
                            String productSpecificationName = shipmentItem.path(PRODUCT)
                                    .path(PRODUCT_SPECIFICATION)
                                    .path("name").asText();

                            // Navigate to product characteristics
                            JsonNode productCharacteristics = shipmentItem
                                    .path(PRODUCT)
                                    .path(PRODUCT_CHARACTERISTIC);

                            // Extract relevant parameters based on characteristic names and product type
                            for (JsonNode characteristic : productCharacteristics) {
                                String name = characteristic.path("name").asText();
                                String value = characteristic.path("value").asText();

                                // Apply filtering based on productSpecification.name
                                if ("NTU PS".equals(productSpecificationName)) {
                                    switch (name) {
                                        case NTU_SIZE: ntuSize = value; break;
                                        case DISTANCE: distance = value; break;
                                        case NETWORK_ELEMENT: networkElement = value; break;
                                    }
                                } else if ("UNI".equals(productSpecificationName)) {
                                    switch (name) {
                                        case NTU_REQUIRED_REQUEST: ntuRequired = value; break;
                                        case UNI_PORT_CAPACITY_REQUEST: uniPortCapacity = value; break;
                                        case INTERFACE_TYPE: uniInterfaceType = value; break;
                                        case DISTANCE: distance = value; break;
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
                    if (INSTALLATION_METHOD.equals(characteristic.path("name").asText())) {
                        installationMethod = characteristic.path("value").asText();
                        break;
                    }
                }
            }

            // Apply NTURequired logic: If NTURequired is "No", set ntuSize to "0"
            if ("No".equalsIgnoreCase(ntuRequired)) {
                ntuSize = "0";
            }

            // Prepare output variables to send back
            Map<String, Object> output = Map.of(
                    NETWORK_ELEMENT, networkElement,
                    DISTANCE, distance,
                    NTU_REQUIRED, ntuRequired,
                    NTU_SIZE, ntuSize,
                    UNI_PORT_CAPACITY, uniPortCapacity,
                    UNI_INTERFACE_TYPE, uniInterfaceType,
                    STATE_OR_PROVINCE, stateOrProvince,
                    INSTALLATION_METHOD, installationMethod
            );

            // Complete the job with extracted variables
            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            logger.info("Job completed with extracted parameters: {}", output);

        } catch (Exception e) {
            // Log and handle exceptions by sending an error message
            logger.error("Error processing fetchDbQueryParams job", e);
            Map<String, Object> output = new HashMap<>();
            output.put(ERROR_MESSAGE, e.getMessage());

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        }
    }
}
