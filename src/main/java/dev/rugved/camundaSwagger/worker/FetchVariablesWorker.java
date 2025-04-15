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

import static dev.rugved.camundaSwagger.util.Constants.JOB_TYPE_FETCHVARIABLES;

/**
 * Worker responsible for extracting and processing variables from the process instance.
 * This worker is triggered for job type "fetchVariables".
 * Modified to be case-insensitive for field and characteristic names.
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
                // Only log correlation ID without additional sensitive data
                logger.debug("Successfully extracted correlationId for job: {}", job.getKey());
            } catch (Exception e) {
                logger.warn("Could not extract correlationId for job: {}: {}", job.getKey(), e.getMessage());
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

            // Log success information
            logger.info("Successfully extracted variables - jobKey: {}, installationMethod: {}, productFields: {}",
                    job.getKey(), installationMethod, String.join(", ", productDetails.keySet()));

            // Complete the job with extracted variables
            client.newCompleteCommand(job.getKey())
                    .variables(output)
                    .send()
                    .join();

            logger.debug("Completed fetchVariables job with key: {}", job.getKey());

        } catch (Exception e) {
            logger.error("Error processing {} job: {}", JOB_TYPE_FETCHVARIABLES, e.getMessage());

            // Handle error and complete the job with error information
            Map<String, Object> errorOutput = errorHandlerService.handleError(e, JOB_TYPE_FETCHVARIABLES);

            // Always include correlationId in the error response if available
            if (correlationId != null) {
                errorOutput.put("correlationId", correlationId);
                logger.debug("Adding correlationId to error response for job: {}", job.getKey());
            } else {
                // Try one more time to extract correlation ID directly from variables
                try {
                    List<Map<String, Object>> characteristics =
                            getRequiredListCaseInsensitive(variables, "shippingOrderCharacteristic",
                                    "Missing or invalid input: 'shippingOrderCharacteristic' is required and must be a list.");

                    if (characteristics != null) {
                        for (Map<String, Object> characteristic : characteristics) {
                            if (isFieldMatching(characteristic, "name", "CorrelationId")) {
                                correlationId = (String) characteristic.get(getMatchingKey(characteristic, "name"));
                                if (correlationId != null) {
                                    errorOutput.put("correlationId", correlationId);
                                    logger.debug("Found correlationId in final attempt for job: {}", job.getKey());
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("Final attempt to extract correlationId failed for job: {}: {}",
                            job.getKey(), ex.getMessage());
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
        List<Map<String, Object>> relatedParty = getRequiredListCaseInsensitive(variables, "relatedParty",
                "Missing or invalid input: 'relatedParty' is required and must be a list.");

        // Find the site address in related party
        for (Map<String, Object> party : relatedParty) {
            if (isFieldMatching(party, "role", "siteAddress")) {
                // Get contact medium for site address
                List<Map<String, Object>> contactMedium = getRequiredListCaseInsensitive(party, "contactMedium",
                        "Missing or invalid input: 'contactMedium' is required for siteAddress.");

                // Get characteristic with state/province
                Map<String, Object> characteristic = getRequiredMapCaseInsensitive(contactMedium.get(0), "characteristic",
                        "Missing or invalid input: 'characteristic' is required for contactMedium.");

                // Get state/province value - try different possible field names
                String stateOrProvince = getFieldValueIgnoreCase(characteristic,
                        new String[]{"stateOrProvince", "state", "province", "stateorprovince"});

                if (stateOrProvince != null) {
                    return stateOrProvince;
                }

                throw new IllegalArgumentException(
                        "Missing or invalid input: 'stateOrProvince', 'state' or 'province' is required.");
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
        List<Map<String, Object>> characteristics = getRequiredListCaseInsensitive(variables, "shippingOrderCharacteristic",
                "Missing or invalid input: 'shippingOrderCharacteristic' is required and must be a list.");

        // Find the correlation ID characteristic
        for (Map<String, Object> characteristic : characteristics) {
            if (isFieldMatching(characteristic, "name", "CorrelationId")) {
                String value = getFieldValueIgnoreCase(characteristic, new String[]{"value"});
                if (value != null) {
                    return value;
                }
                throw new IllegalArgumentException("Missing or invalid input: 'CorrelationId' value is required.");
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
        List<Map<String, Object>> characteristics = getRequiredListCaseInsensitive(variables, "shippingOrderCharacteristic",
                "Missing or invalid input: 'shippingOrderCharacteristic' is required and must be a list.");

        // Find the installation method characteristic
        for (Map<String, Object> characteristic : characteristics) {
            if (isFieldMatching(characteristic, "name", "InstallationMethod")) {
                String value = getFieldValueIgnoreCase(characteristic, new String[]{"value"});
                if (value != null) {
                    return value;
                }
                throw new IllegalArgumentException("Missing or invalid input: 'InstallationMethod' value is required.");
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
        List<Map<String, Object>> shippingOrderItems = getRequiredListCaseInsensitive(variables, "shippingOrderItem",
                "Missing or invalid input: 'shippingOrderItem' is required and must be a list.");

        boolean targetProductFound = false;

        // Process shipping order items
        for (Map<String, Object> shippingOrderItem : shippingOrderItems) {
            // Get shipment
            Map<String, Object> shipment = getRequiredMapCaseInsensitive(shippingOrderItem, "shipment",
                    "Missing or invalid input: 'shipment' is required in shippingOrderItem.");

            // Get shipment items
            List<Map<String, Object>> shipmentItems = getRequiredListCaseInsensitive(shipment, "shipmentItem",
                    "Missing or invalid input: 'shipmentItem' is required and must be a list.");

            // Process each shipment item
            for (Map<String, Object> shipmentItem : shipmentItems) {
                // Get product
                Map<String, Object> product = getRequiredMapCaseInsensitive(shipmentItem, "product",
                        "Missing or invalid input: 'product' is required in shipmentItem.");

                // Get product specification
                Map<String, Object> productSpecification = getRequiredMapCaseInsensitive(product, "productSpecification",
                        "Missing or invalid input: 'productSpecification' is required in product.");

                // Get product specification ID
                String productSpecId = getFieldValueIgnoreCase(productSpecification, new String[]{"id"});
                if (productSpecId == null) {
                    throw new IllegalArgumentException(
                            "Missing or invalid input: 'id' is required in productSpecification.");
                }

                // Get product characteristics
                List<Map<String, Object>> productCharacteristics = getRequiredListCaseInsensitive(product, "productCharacteristic",
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
                        if (isFieldMatching(characteristic, "name", "ntuSize")) {
                            String value = getFieldValueIgnoreCase(characteristic, new String[]{"value"});
                            if (value != null) {
                                try {
                                    ntuSizeFromNTUProduct = Integer.parseInt(value);
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid ntuSize format: {}", value);
                                }
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
        if ("Yes".equalsIgnoreCase((String) productDetails.get("ntuRequired")) && ntuSizeFromNTUProduct != null) {
            ntuSize = ntuSizeFromNTUProduct;
        } else if ("Yes".equalsIgnoreCase((String) productDetails.get("ntuRequired")) && ntuSizeFromNTUProduct == null) {
            throw new IllegalArgumentException(
                    "Missing or invalid input: 'ntuSize' is required when 'NTURequired' is 'Yes'.");
        }

        productDetails.put("ntuSize", ntuSize.toString());
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
            String name = getFieldValueIgnoreCase(characteristic, new String[]{"name"});
            String value = getFieldValueIgnoreCase(characteristic, new String[]{"value"});

            if (name == null || value == null) {
                continue;
            }

            // Case-insensitive matching for characteristic names
            if (name.equalsIgnoreCase("InterfaceType")) {
                productDetails.put("uniInterfaceType", value);
            } else if (name.equalsIgnoreCase("NTURequired")) {
                productDetails.put("ntuRequired", value);
            } else if (name.equalsIgnoreCase("UNIPortCapacity") || name.equalsIgnoreCase("UNISpeed")) {
                // Accept either UNIPortCapacity or UNISpeed and use internally as uniPortCapacity
                productDetails.put("uniPortCapacity", value);
            } else if (name.equalsIgnoreCase("distance")) {
                productDetails.put("distance", value);
            } else if (name.equalsIgnoreCase("networkElement")) {
                productDetails.put("networkElement", value);
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
     * Helper method to get a required list value from a map with case-insensitive key matching.
     *
     * @param map The source map
     * @param key The key to retrieve (case-insensitive)
     * @param errorMsg The error message if the value is missing or not a list
     * @return The list value
     * @throws IllegalArgumentException if the value is missing or not a list
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getRequiredListCaseInsensitive(Map<String, Object> map, String key, String errorMsg) {
        String matchingKey = getMatchingKey(map, key);
        if (matchingKey == null) {
            throw new IllegalArgumentException(errorMsg);
        }

        Object value = map.get(matchingKey);
        if (value == null || !(value instanceof List)) {
            throw new IllegalArgumentException(errorMsg);
        }

        return (List<Map<String, Object>>) value;
    }

    /**
     * Helper method to get a required map value from a map with case-insensitive key matching.
     *
     * @param map The source map
     * @param key The key to retrieve (case-insensitive)
     * @param errorMsg The error message if the value is missing or not a map
     * @return The map value
     * @throws IllegalArgumentException if the value is missing or not a map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getRequiredMapCaseInsensitive(Map<String, Object> map, String key, String errorMsg) {
        String matchingKey = getMatchingKey(map, key);
        if (matchingKey == null) {
            throw new IllegalArgumentException(errorMsg);
        }

        Object value = map.get(matchingKey);
        if (value == null || !(value instanceof Map)) {
            throw new IllegalArgumentException(errorMsg);
        }

        return (Map<String, Object>) value;
    }

    /**
     * Finds a matching key in a map regardless of case.
     *
     * @param map The map to search in
     * @param targetKey The key to look for (case-insensitive)
     * @return The actual key from the map that matches the target key, or null if not found
     */
    private String getMatchingKey(Map<String, Object> map, String targetKey) {
        if (map == null || targetKey == null) {
            return null;
        }

        // First try exact match for efficiency
        if (map.containsKey(targetKey)) {
            return targetKey;
        }

        // Otherwise, do case-insensitive search
        return map.keySet().stream()
                .filter(key -> key.equalsIgnoreCase(targetKey))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if a field in a map matches the expected value, ignoring case.
     *
     * @param map The map containing the field
     * @param fieldName The name of the field to check (case-insensitive)
     * @param expectedValue The expected value (case-insensitive)
     * @return true if the field exists and matches the expected value, false otherwise
     */
    private boolean isFieldMatching(Map<String, Object> map, String fieldName, String expectedValue) {
        String matchingKey = getMatchingKey(map, fieldName);
        if (matchingKey == null) {
            return false;
        }

        Object value = map.get(matchingKey);
        return value != null && value.toString().equalsIgnoreCase(expectedValue);
    }

    /**
     * Gets a field value from a map, trying multiple possible field names in order.
     *
     * @param map The map to get the value from
     * @param possibleFields Array of possible field names to try (case-insensitive)
     * @return The field value if found, null otherwise
     */
    private String getFieldValueIgnoreCase(Map<String, Object> map, String[] possibleFields) {
        if (map == null || possibleFields == null) {
            return null;
        }

        for (String field : possibleFields) {
            String matchingKey = getMatchingKey(map, field);
            if (matchingKey != null && map.get(matchingKey) != null) {
                return map.get(matchingKey).toString();
            }
        }

        return null;
    }
}