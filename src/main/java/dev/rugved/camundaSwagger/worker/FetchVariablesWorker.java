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
    private static final String NTU_PRODUCT_SPEC_ID = "84b0d8a2-1b90-47ab-b8d8-f119ea330bef";

    @JobWorker(type = JOB_TYPE_FETCHVARIABLES)
    public void fetchDbQueryParams(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = job.getVariablesAsMap();

            String stateOrProvince = extractStateOrProvince(variables);
            String installationMethod = extractInstallationMethod(variables);
            Map<String, Object> productDetails = extractProductDetails(variables);

            Map<String, Object> output = new HashMap<>();
            output.put("stateOrProvince", stateOrProvince);
            output.put("installationMethod", installationMethod);
            output.putAll(productDetails);

            logger.info("Fetched variables" + output.toString());

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        } catch (Exception e) {
            logger.error("Error processing job variables", e);
            Map<String, Object> errorOutput = Map.of("errorMessage", e.getMessage(), "errorCode", "CAM-" + job.getKey());
            client.newCompleteCommand(job.getKey())
                    .variables(errorOutput)
                    .send()
                    .join();
        }
    }

    private String extractStateOrProvince(Map<String, Object> variables) {

        List<Map<String, Object>> relatedParty = (List<Map<String, Object>>) variables.get("relatedParty");
        if (relatedParty == null || relatedParty.isEmpty()) {
            throw new IllegalArgumentException("Missing or invalid input: 'relatedParty' is required and must be a list.");
        }
        for (Map<String, Object> party : relatedParty) {
            if ("siteAddress".equals(party.get("role"))) {
                List<Map<String, Object>> contactMedium = (List<Map<String, Object>>) party.get("contactMedium");
                if (contactMedium != null && !contactMedium.isEmpty()) {
                    Map<String, Object> characteristic = (Map<String, Object>) contactMedium.get(0).get("characteristic");
                    return (String) characteristic.get("stateOrProvince");
                }
            }
        }
        return null;
    }

    private String extractInstallationMethod(Map<String, Object> variables) {
        List<Map<String, Object>> characteristics = (List<Map<String, Object>>) variables.get("shippingOrderCharacteristic");
        for (Map<String, Object> characteristic : characteristics) {
            if ("InstallationMethod".equals(characteristic.get("name"))) {
                return (String) characteristic.get("value");
            }
        }
        return null;
    }

    private Map<String, Object> extractProductDetails(Map<String, Object> variables) {
        Map<String, Object> productDetails = new HashMap<>();
        Integer ntuSize = 0; // Default value
        Integer ntuSizeFromNTUProduct = null; // Store NTU size separately if found in NTU product block

        List<Map<String, Object>> shippingOrderItems = (List<Map<String, Object>>) variables.get("shippingOrderItem");

        for (Map<String, Object> shippingOrderItem : shippingOrderItems) {
            Map<String, Object> shipment = (Map<String, Object>) shippingOrderItem.get("shipment");
            List<Map<String, Object>> shipmentItems = (List<Map<String, Object>>) shipment.get("shipmentItem");

            for (Map<String, Object> shipmentItem : shipmentItems) {
                Map<String, Object> product = (Map<String, Object>) shipmentItem.get("product");
                Map<String, Object> productSpecification = (Map<String, Object>) product.get("productSpecification");
                String productSpecId = (String) productSpecification.get("id");

                List<Map<String, Object>> productCharacteristics = (List<Map<String, Object>>) product.get("productCharacteristic");

                if (TARGET_PRODUCT_SPEC_ID.equals(productSpecId)) {
                    // Fetch UNI Product Details
                    for (Map<String, Object> characteristic : productCharacteristics) {
                        String name = (String) characteristic.get("name");
                        String value = (String) characteristic.get("value");

                        switch (name) {
                            case "InterfaceType":
                                productDetails.put("uniInterfaceType", value);
                                break;
                            case "NTURequired":
                                productDetails.put("ntuRequired", value);
                                if ("No".equals(value)) {
                                    ntuSize = 0;
                                }
                                break;
                            case "UNIPortCapacity":
                                productDetails.put("uniPortCapacity", value);
                                break;
                            case "distance":
                                productDetails.put("distance", value);
                                break;
                            case "networkElement":
                                productDetails.put("networkElement", value);
                                break;
                        }
                    }
                } else if (NTU_PRODUCT_SPEC_ID.equals(productSpecId)) {
                    // Fetch NTU Size from NTU Product
                    for (Map<String, Object> characteristic : productCharacteristics) {
                        if ("ntuSize".equals(characteristic.get("name"))) {
                            ntuSizeFromNTUProduct = Integer.parseInt((String) characteristic.get("value"));
                        }
                    }
                }
            }
        }

        // If NTU is required and NTU size was found in NTU product, update it
        if ("Yes".equals(productDetails.get("ntuRequired")) && ntuSizeFromNTUProduct != null) {
            ntuSize = ntuSizeFromNTUProduct;
        }

        productDetails.put("ntuSize", ntuSize);
        return productDetails;
    }

}
