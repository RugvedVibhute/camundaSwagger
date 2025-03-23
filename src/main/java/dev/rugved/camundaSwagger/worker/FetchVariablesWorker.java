package dev.rugved.camundaSwagger.worker;

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
public class FetchVariablesWorker {

    private static final Logger logger = LoggerFactory.getLogger(FetchVariablesWorker.class);
    private static final String JOB_TYPE_FETCHVARIABLES = "fetchVariables";
    private static final String TARGET_PRODUCT_SPEC_ID = "601c8c38-07c6-4deb-b473-f15cd843b712";

    @JobWorker(type = JOB_TYPE_FETCHVARIABLES)
    public void fetchDbQueryParams(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = job.getVariablesAsMap();

            Map<String, Object> output = new HashMap<>();
            output.put("stateOrProvince", extractStateOrProvince(variables));
            output.put("InstallationMethod", extractInstallationMethod(variables));
            extractProductCharacteristics(variables, output);

            logger.info("Extracted Variables: {}", output);

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        } catch (Exception e) {
            logger.error("Error processing job variables", e);
            Map<String, Object> errorOutput = Map.of("errorMessage", e.getMessage(), "errorCode", "CAM-" + job.getKey());
            client.newFailCommand(job.getKey()).retries(0).variables(errorOutput).send().join();
        }
    }

    private String extractStateOrProvince(Map<String, Object> variables) {
        List<Map<String, Object>> relatedParty = (List<Map<String, Object>>) variables.get("relatedParty");
        if (relatedParty != null) {
            for (Map<String, Object> party : relatedParty) {
                List<Map<String, Object>> contactMedium = (List<Map<String, Object>>) party.get("contactMedium");
                if (contactMedium != null) {
                    for (Map<String, Object> contact : contactMedium) {
                        Map<String, Object> characteristic = (Map<String, Object>) contact.get("characteristic");
                        if (characteristic != null && characteristic.containsKey("stateOrProvince")) {
                            return (String) characteristic.get("stateOrProvince");
                        }
                    }
                }
            }
        }
        return null;
    }

    private String extractInstallationMethod(Map<String, Object> variables) {
        List<Map<String, Object>> characteristics = (List<Map<String, Object>>) variables.get("shippingOrderCharacteristic");
        if (characteristics != null) {
            for (Map<String, Object> characteristic : characteristics) {
                if ("InstallationMethod".equals(characteristic.get("name"))) {
                    return (String) characteristic.get("value");
                }
            }
        }
        return null;
    }

    private void extractProductCharacteristics(Map<String, Object> variables, Map<String, Object> output) {
        List<Map<String, Object>> shippingOrderItems = (List<Map<String, Object>>) variables.get("shippingOrderItem");
        if (shippingOrderItems != null) {
            for (Map<String, Object> orderItem : shippingOrderItems) {
                List<Map<String, Object>> shipmentItems = (List<Map<String, Object>>) ((Map<String, Object>) orderItem.get("shipment")).get("shipmentItem");
                if (shipmentItems != null) {
                    for (Map<String, Object> shipmentItem : shipmentItems) {
                        Map<String, Object> product = (Map<String, Object>) shipmentItem.get("product");
                        if (product != null) {
                            Map<String, Object> productSpec = (Map<String, Object>) product.get("productSpecification");
                            if (productSpec != null && TARGET_PRODUCT_SPEC_ID.equals(productSpec.get("id"))) {
                                List<Map<String, Object>> characteristics = (List<Map<String, Object>>) product.get("productCharacteristic");
                                if (characteristics != null) {
                                    output.put("networkElement", getCharacteristicValue(characteristics, "networkElement"));
                                    output.put("distance", getCharacteristicValue(characteristics, "distance"));
                                    output.put("ntuRequired", getCharacteristicValue(characteristics, "NTURequired"));
                                    output.put("ntuSize", "No".equalsIgnoreCase((String) output.get("ntuRequired")) ? "0" : null);
                                    output.put("uniPortCapacity", getCharacteristicValue(characteristics, "UNIPortCapacity"));
                                    output.put("uniInterfaceType", getCharacteristicValue(characteristics, "InterfaceType"));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private String getCharacteristicValue(List<Map<String, Object>> characteristics, String name) {
        for (Map<String, Object> characteristic : characteristics) {
            if (name.equals(characteristic.get("name"))) {
                return (String) characteristic.get("value");
            }
        }
        return null;
    }
}
