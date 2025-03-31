package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.service.ErrorHandlerService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Component
public class FetchVariablesWorker {

    private static final Logger logger = LoggerFactory.getLogger(FetchVariablesWorker.class);
    private static final String TARGET_PRODUCT_SPEC_ID = "601c8c38-07c6-4deb-b473-f15cd843b712";
    private static final String NTU_PRODUCT_SPEC_ID = "84b0d8a2-1b90-47ab-b8d8-f119ea330bef";

    private final ErrorHandlerService errorHandlerService;

    public FetchVariablesWorker(ErrorHandlerService errorHandlerService) {
        this.errorHandlerService = errorHandlerService;
    }

    @JobWorker(type = JOB_TYPE_FETCHVARIABLES)
    public void fetchDbQueryParams(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = job.getVariablesAsMap();

            String stateOrProvince = extractStateOrProvince(variables);
            String installationMethod = extractInstallationMethod(variables);
            String correlationId = extractCorrelationId(variables);
            Map<String, Object> productDetails = extractProductDetails(variables);

            Map<String, Object> output = new HashMap<>();
            output.put("correlationId", correlationId);
            output.put("stateOrProvince", stateOrProvince);
            output.put("installationMethod", installationMethod);
            output.putAll(productDetails);

            logger.info("Fetched variables" + output.toString());

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        } catch (Exception e) {
            logger.error("Error processing {} job: {}", JOB_TYPE_FETCHVARIABLES, e.getMessage(), e);
            Map<String, Object> errorOutput = errorHandlerService.handleError(e, JOB_TYPE_FETCHVARIABLES);
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
                if (contactMedium == null || contactMedium.isEmpty()) {
                    throw new IllegalArgumentException("Missing or invalid input: 'contactMedium' is required for siteAddress.");
                }
                Map<String, Object> characteristic = (Map<String, Object>) contactMedium.get(0).get("characteristic");
                if (characteristic == null || characteristic.get("stateOrProvince") == null) {
                    throw new IllegalArgumentException("Missing or invalid input: 'stateOrProvince' is required.");
                }
                return (String) characteristic.get("stateOrProvince");
            }
        }
        throw new IllegalArgumentException("Missing or invalid input: 'siteAddress' role not found in relatedParty.");
    }

    private String extractCorrelationId(Map<String, Object> variables) {
        List<Map<String, Object>> characteristics = (List<Map<String, Object>>) variables.get("shippingOrderCharacteristic");
        if (characteristics == null || characteristics.isEmpty()) {
            throw new IllegalArgumentException("Missing or invalid input: 'shippingOrderCharacteristic' is required and must be a list.");
        }
        for (Map<String, Object> characteristic : characteristics) {
            if ("CorrelationId".equals(characteristic.get("name"))) {
                return (String) characteristic.get("value");
            }
        }
        throw new IllegalArgumentException("Missing or invalid input: 'InstallationMethod' characteristic not found.");
    }

    private String extractInstallationMethod(Map<String, Object> variables) {
        List<Map<String, Object>> characteristics = (List<Map<String, Object>>) variables.get("shippingOrderCharacteristic");
        if (characteristics == null || characteristics.isEmpty()) {
            throw new IllegalArgumentException("Missing or invalid input: 'shippingOrderCharacteristic' is required and must be a list.");
        }
        for (Map<String, Object> characteristic : characteristics) {
            if ("InstallationMethod".equals(characteristic.get("name"))) {
                return (String) characteristic.get("value");
            }
        }
        throw new IllegalArgumentException("Missing or invalid input: 'InstallationMethod' characteristic not found.");
    }

    private Map<String, Object> extractProductDetails(Map<String, Object> variables) {
        Map<String, Object> productDetails = new HashMap<>();
        Integer ntuSize = 0; // Default value
        Integer ntuSizeFromNTUProduct = null; // Store NTU size separately if found in NTU product block

        List<Map<String, Object>> shippingOrderItems = (List<Map<String, Object>>) variables.get("shippingOrderItem");
        if (shippingOrderItems == null || shippingOrderItems.isEmpty()) {
            throw new IllegalArgumentException("Missing or invalid input: 'shippingOrderItem' is required and must be a list.");
        }

        boolean targetProductFound = false;

        for (Map<String, Object> shippingOrderItem : shippingOrderItems) {
            Map<String, Object> shipment = (Map<String, Object>) shippingOrderItem.get("shipment");
            if (shipment == null) {
                throw new IllegalArgumentException("Missing or invalid input: 'shipment' is required in shippingOrderItem.");
            }

            List<Map<String, Object>> shipmentItems = (List<Map<String, Object>>) shipment.get("shipmentItem");
            if (shipmentItems == null || shipmentItems.isEmpty()) {
                throw new IllegalArgumentException("Missing or invalid input: 'shipmentItem' is required and must be a list.");
            }

            for (Map<String, Object> shipmentItem : shipmentItems) {
                Map<String, Object> product = (Map<String, Object>) shipmentItem.get("product");
                if (product == null) {
                    throw new IllegalArgumentException("Missing or invalid input: 'product' is required in shipmentItem.");
                }

                Map<String, Object> productSpecification = (Map<String, Object>) product.get("productSpecification");
                if (productSpecification == null) {
                    throw new IllegalArgumentException("Missing or invalid input: 'productSpecification' is required in product.");
                }

                String productSpecId = (String) productSpecification.get("id");
                if (productSpecId == null) {
                    throw new IllegalArgumentException("Missing or invalid input: 'id' is required in productSpecification.");
                }

                List<Map<String, Object>> productCharacteristics = (List<Map<String, Object>>) product.get("productCharacteristic");
                if (productCharacteristics == null || productCharacteristics.isEmpty()) {
                    throw new IllegalArgumentException("Missing or invalid input: 'productCharacteristic' is required and must be a list.");
                }

                if (TARGET_PRODUCT_SPEC_ID.equals(productSpecId)) {
                    // Fetch UNI Product Details
                    targetProductFound = true;
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

        if (!targetProductFound) {
            throw new IllegalArgumentException("Missing or invalid input: Target product with spec ID " + TARGET_PRODUCT_SPEC_ID + " not found.");
        }

        // Validate required product details
        if (!productDetails.containsKey("uniInterfaceType")) {
            throw new IllegalArgumentException("Missing or invalid input: 'InterfaceType' characteristic not found.");
        }
        if (!productDetails.containsKey("ntuRequired")) {
            throw new IllegalArgumentException("Missing or invalid input: 'NTURequired' characteristic not found.");
        }
        if (!productDetails.containsKey("uniPortCapacity")) {
            throw new IllegalArgumentException("Missing or invalid input: 'UNIPortCapacity' characteristic not found.");
        }
        if (!productDetails.containsKey("distance")) {
            throw new IllegalArgumentException("Missing or invalid input: 'distance' characteristic not found.");
        }
        if (!productDetails.containsKey("networkElement")) {
            throw new IllegalArgumentException("Missing or invalid input: 'networkElement' characteristic not found.");
        }

        // If NTU is required and NTU size was found in NTU product, update it
        if ("Yes".equals(productDetails.get("ntuRequired")) && ntuSizeFromNTUProduct != null) {
            ntuSize = ntuSizeFromNTUProduct;
        } else if ("Yes".equals(productDetails.get("ntuRequired")) && ntuSizeFromNTUProduct == null) {
            throw new IllegalArgumentException("Missing or invalid input: 'ntuSize' is required when 'NTURequired' is 'Yes'.");
        }

        productDetails.put("ntuSize", ntuSize);
        return productDetails;
    }
}