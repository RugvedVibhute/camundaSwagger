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
import java.util.Optional;

import static dev.rugved.camundaSwagger.util.Constants.*;

/**
 * Worker responsible for extracting and processing variables from the process instance.
 * This worker is triggered for job type "fetchVariables".
 */
@Component
public class FetchVariablesWorker {

    private static final Logger logger = LoggerFactory.getLogger(FetchVariablesWorker.class);

    // Product specification IDs
    private static final String TARGET_PRODUCT_SPEC_ID = "601c8c38-07c6-4deb-b473-f15cd843b712";
    private static final String NTU_PRODUCT_SPEC_ID = "84b0d8a2-1b90-47ab-b8d8-f119ea330bef";

    private final ErrorHandlerService errorHandlerService;

    public FetchVariablesWorker(ErrorHandlerService errorHandlerService) {
        this.errorHandlerService = errorHandlerService;
    }

    /**
     * Extracts variables from the job context and prepares them for the next steps in the process.
     *
     * @param client The Zeebe job client
     * @param job The activated job with variables
     */
    @JobWorker(type = JOB_TYPE_FETCHVARIABLES)
    public void fetchDbQueryParams(final JobClient client, final ActivatedJob job) {
        logger.debug("Starting fetchVariables job processing with key: {}", job.getKey());
        Map<String, Object> variables = job.getVariablesAsMap();
        String correlationId = null;

        try {
            // First try to extract the correlation ID, even if other processing fails
            try {
                correlationId = extractCorrelationId(variables);
                logger.debug("Successfully extracted correlationId: {}", correlationId);
            } catch (Exception e) {
                logger.warn("Could not extract correlationId: {}", e.getMessage());
                // Continue processing - we'll handle missing correlationId in the outer catch block
            }

            // Extract other required variables
            String stateOrProvince = extractStateOrProvince(variables);
            String installationMethod = extractInstallationMethod(variables);
            if (correlationId == null) {
                // Try again if we couldn't get it earlier
                correlationId = extractCorrelationId(variables);
            }
            Map<String, Object> productDetails = extractProductDetails(variables);

            // Prepare output map
            Map<String, Object> output = new HashMap<>();
            output.put("correlationId", correlationId);
            output.put("stateOrProvince", stateOrProvince);
            output.put("installationMethod", installationMethod);
            output.putAll(productDetails);

            logger.info("Successfully extracted variables: {}", output);

            // Complete the job with extracted variables
            client.newCompleteCommand(job.getKey())
                    .variables(output)
                    .send()
                    .join();

            logger.debug("Completed fetchVariables job with key: {}", job.getKey());

        } catch (Exception e) {
            logger.error("Error processing {} job: {}", JOB_TYPE_FETCHVARIABLES, e.getMessage(), e);

            // Handle error and complete the job with error information
            Map<String, Object> errorOutput = errorHandlerService.handleError(e, JOB_TYPE_FETCHVARIABLES);

            // Always include correlationId in the error response if available
            if (correlationId != null) {
                errorOutput.put("correlationId", correlationId);
                logger.debug("Adding correlationId to error response: {}", correlationId);
            } else {
                // Try one more time to extract correlation ID directly from variables
                try {
                    List<Map<String, Object>> characteristics =
                            (List<Map<String, Object>>) variables.get("shippingOrderCharacteristic");
                    if (characteristics != null) {
                        for (Map<String, Object> characteristic : characteristics) {
                            if ("CorrelationId".equals(characteristic.get("name"))) {
                                correlationId = (String) characteristic.get("value");
                                if (correlationId != null) {
                                    errorOutput.put("correlationId", correlationId);
                                    logger.debug("Found correlationId in final attempt: {}", correlationId);
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("Final attempt to extract correlationId failed: {}", ex.getMessage());
                }
            }

            client.newCompleteCommand(job.getKey())
                    .variables(errorOutput)
                    .send()
                    .join();

            logger.debug("Completed fetchVariables job with error, job key: {}", job.getKey());
        }
    }

    /**
     * Extracts the state or province from the related party information in the variables.
     *
     * @param variables The variables map containing related party information
     * @return The extracted state or province value
     * @throws IllegalArgumentException if the required data cannot be found
     */
    private String extractStateOrProvince(Map<String, Object> variables) {
        // Check for related party array
        List<Map<String, Object>> relatedParty = getRequiredListValue(variables, "relatedParty",
                "Missing or invalid input: 'relatedParty' is required and must be a list.");

        // Find the site address in related party
        for (Map<String, Object> party : relatedParty) {
            if ("siteAddress".equals(party.get("role"))) {
                // Get contact medium for site address
                List<Map<String, Object>> contactMedium = getRequiredListValue(party, "contactMedium",
                        "Missing or invalid input: 'contactMedium' is required for siteAddress.");

                // Get characteristic with state/province
                Map<String, Object> characteristic = getRequiredMapValue(contactMedium.get(0), "characteristic",
                        "Missing or invalid input: 'characteristic' is required for contactMedium.");

                // Get state/province value
                String stateOrProvince = Optional.ofNullable(characteristic.get("stateOrProvince"))
                        .map(Object::toString)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Missing or invalid input: 'stateOrProvince' is required."));

                return stateOrProvince;
            }
        }

        throw new IllegalArgumentException("Missing or invalid input: 'siteAddress' role not found in relatedParty.");
    }

    /**
     * Extracts the correlation ID from shipping order characteristics.
     *
     * @param variables The variables map containing shipping order characteristics
     * @return The extracted correlation ID
     * @throws IllegalArgumentException if the correlation ID cannot be found
     */
    private String extractCorrelationId(Map<String, Object> variables) {
        // Get shipping order characteristics
        List<Map<String, Object>> characteristics = getRequiredListValue(variables, "shippingOrderCharacteristic",
                "Missing or invalid input: 'shippingOrderCharacteristic' is required and must be a list.");

        // Find the correlation ID characteristic
        for (Map<String, Object> characteristic : characteristics) {
            if ("CorrelationId".equals(characteristic.get("name"))) {
                return Optional.ofNullable(characteristic.get("value"))
                        .map(Object::toString)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Missing or invalid input: 'CorrelationId' value is required."));
            }
        }

        throw new IllegalArgumentException("Missing or invalid input: 'CorrelationId' characteristic not found.");
    }

    /**
     * Extracts the installation method from shipping order characteristics.
     *
     * @param variables The variables map containing shipping order characteristics
     * @return The extracted installation method
     * @throws IllegalArgumentException if the installation method cannot be found
     */
    private String extractInstallationMethod(Map<String, Object> variables) {
        // Get shipping order characteristics
        List<Map<String, Object>> characteristics = getRequiredListValue(variables, "shippingOrderCharacteristic",
                "Missing or invalid input: 'shippingOrderCharacteristic' is required and must be a list.");

        // Find the installation method characteristic
        for (Map<String, Object> characteristic : characteristics) {
            if ("InstallationMethod".equals(characteristic.get("name"))) {
                return Optional.ofNullable(characteristic.get("value"))
                        .map(Object::toString)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Missing or invalid input: 'InstallationMethod' value is required."));
            }
        }

        throw new IllegalArgumentException("Missing or invalid input: 'InstallationMethod' characteristic not found.");
    }

    /**
     * Extracts product details from shipping order items.
     *
     * @param variables The variables map containing shipping order items
     * @return A map of extracted product details
     * @throws IllegalArgumentException if required product details cannot be found
     */
    private Map<String, Object> extractProductDetails(Map<String, Object> variables) {
        Map<String, Object> productDetails = new HashMap<>();
        Integer ntuSize = 0; // Default value
        Integer ntuSizeFromNTUProduct = null; // Store NTU size separately if found in NTU product block

        // Get shipping order items
        List<Map<String, Object>> shippingOrderItems = getRequiredListValue(variables, "shippingOrderItem",
                "Missing or invalid input: 'shippingOrderItem' is required and must be a list.");

        boolean targetProductFound = false;

        // Process shipping order items
        for (Map<String, Object> shippingOrderItem : shippingOrderItems) {
            // Get shipment
            Map<String, Object> shipment = getRequiredMapValue(shippingOrderItem, "shipment",
                    "Missing or invalid input: 'shipment' is required in shippingOrderItem.");

            // Get shipment items
            List<Map<String, Object>> shipmentItems = getRequiredListValue(shipment, "shipmentItem",
                    "Missing or invalid input: 'shipmentItem' is required and must be a list.");

            // Process each shipment item
            for (Map<String, Object> shipmentItem : shipmentItems) {
                // Get product
                Map<String, Object> product = getRequiredMapValue(shipmentItem, "product",
                        "Missing or invalid input: 'product' is required in shipmentItem.");

                // Get product specification
                Map<String, Object> productSpecification = getRequiredMapValue(product, "productSpecification",
                        "Missing or invalid input: 'productSpecification' is required in product.");

                // Get product specification ID
                String productSpecId = Optional.ofNullable(productSpecification.get("id"))
                        .map(Object::toString)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Missing or invalid input: 'id' is required in productSpecification."));

                // Get product characteristics
                List<Map<String, Object>> productCharacteristics = getRequiredListValue(product, "productCharacteristic",
                        "Missing or invalid input: 'productCharacteristic' is required and must be a list.");

                // Process target product specification
                if (TARGET_PRODUCT_SPEC_ID.equals(productSpecId)) {
                    // Fetch UNI Product Details
                    targetProductFound = true;
                    extractTargetProductCharacteristics(productCharacteristics, productDetails);
                }
                // Process NTU product specification
                else if (NTU_PRODUCT_SPEC_ID.equals(productSpecId)) {
                    // Fetch NTU Size from NTU Product
                    for (Map<String, Object> characteristic : productCharacteristics) {
                        if ("ntuSize".equals(characteristic.get("name"))) {
                            String valueStr = (String) characteristic.get("value");
                            if (valueStr != null) {
                                ntuSizeFromNTUProduct = Integer.parseInt(valueStr);
                            }
                        }
                    }
                }
            }
        }

        // Validate target product was found
        if (!targetProductFound) {
            throw new IllegalArgumentException(
                    "Missing or invalid input: Target product with spec ID " + TARGET_PRODUCT_SPEC_ID + " not found.");
        }

        // Validate required product details
        validateRequiredProductDetails(productDetails);

        // Determine NTU size
        if ("Yes".equals(productDetails.get("ntuRequired")) && ntuSizeFromNTUProduct != null) {
            ntuSize = ntuSizeFromNTUProduct;
        } else if ("Yes".equals(productDetails.get("ntuRequired")) && ntuSizeFromNTUProduct == null) {
            throw new IllegalArgumentException(
                    "Missing or invalid input: 'ntuSize' is required when 'NTURequired' is 'Yes'.");
        }

        productDetails.put("ntuSize", ntuSize);
        return productDetails;
    }

    /**
     * Extracts characteristics from target product.
     *
     * @param productCharacteristics List of product characteristics
     * @param productDetails Map to store extracted details
     */
    private void extractTargetProductCharacteristics(List<Map<String, Object>> productCharacteristics,
                                                     Map<String, Object> productDetails) {
        for (Map<String, Object> characteristic : productCharacteristics) {
            String name = (String) characteristic.get("name");
            String value = (String) characteristic.get("value");

            switch (name) {
                case "InterfaceType":
                    productDetails.put("uniInterfaceType", value);
                    break;
                case "NTURequired":
                    productDetails.put("ntuRequired", value);
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
    }

    /**
     * Validates that all required product details are present.
     *
     * @param productDetails Map of product details to validate
     * @throws IllegalArgumentException if any required details are missing
     */
    private void validateRequiredProductDetails(Map<String, Object> productDetails) {
        checkRequiredField(productDetails, "uniInterfaceType", "InterfaceType");
        checkRequiredField(productDetails, "ntuRequired", "NTURequired");
        checkRequiredField(productDetails, "uniPortCapacity", "UNIPortCapacity");
        checkRequiredField(productDetails, "distance", "distance");
        checkRequiredField(productDetails, "networkElement", "networkElement");
    }

    /**
     * Helper method to check if a required field exists in the product details.
     *
     * @param productDetails Map of product details
     * @param fieldName The name of the field to check
     * @param characteristicName The characteristic name for error messages
     * @throws IllegalArgumentException if the field is missing
     */
    private void checkRequiredField(Map<String, Object> productDetails, String fieldName, String characteristicName) {
        if (!productDetails.containsKey(fieldName)) {
            throw new IllegalArgumentException(
                    "Missing or invalid input: '" + characteristicName + "' characteristic not found.");
        }
    }

    /**
     * Helper method to get a required list value from a map.
     *
     * @param map The source map
     * @param key The key to retrieve
     * @param errorMsg The error message if the value is missing or not a list
     * @return The list value
     * @throws IllegalArgumentException if the value is missing or not a list
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getRequiredListValue(Map<String, Object> map, String key, String errorMsg) {
        Object value = map.get(key);
        if (value == null || !(value instanceof List)) {
            throw new IllegalArgumentException(errorMsg);
        }
        return (List<Map<String, Object>>) value;
    }

    /**
     * Helper method to get a required map value from a map.
     *
     * @param map The source map
     * @param key The key to retrieve
     * @param errorMsg The error message if the value is missing or not a map
     * @return The map value
     * @throws IllegalArgumentException if the value is missing or not a map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getRequiredMapValue(Map<String, Object> map, String key, String errorMsg) {
        Object value = map.get(key);
        if (value == null || !(value instanceof Map)) {
            throw new IllegalArgumentException(errorMsg);
        }
        return (Map<String, Object>) value;
    }
}