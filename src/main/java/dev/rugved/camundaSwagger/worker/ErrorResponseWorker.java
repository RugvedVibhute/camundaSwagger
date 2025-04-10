package dev.rugved.camundaSwagger.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Worker responsible for generating standardized error responses.
 */
@Component
public class ErrorResponseWorker {

    private static final Logger logger = LoggerFactory.getLogger(ErrorResponseWorker.class);
    private static final int COMPLETION_TIMEOUT_SECONDS = 10;

    @JobWorker(type = "generateErrorResponse")
    public void buildErrorResponse(final JobClient client, final ActivatedJob job) {
        logger.info("Building error response - jobKey: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        Map<String, Object> errorResponse = new HashMap<>();

        try {
            // Extract error information (from previous workers)
            String errorCode = getStringValue(variables, "errorCode", "CAM-500");
            String errorMessage = getStringValue(variables, "errorMessage", "An unexpected error occurred");
            String correlationId = getStringValue(variables, "correlationId", "unknown");

            // Generate current timestamp in ISO 8601 format
            String eventTime = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);

            // Build the error response structure
            Map<String, Object> shippingOrder = new HashMap<>();
            shippingOrder.put("status", "Rejected");

            // Add shipping order characteristics with error details
            List<Map<String, Object>> characteristics = new ArrayList<>();
            characteristics.add(createCharacteristic("ErrorCode", errorCode, "String"));
            characteristics.add(createCharacteristic("ErrorDescription", errorMessage, "String"));
            shippingOrder.put("shippingOrderCharacteristic", characteristics);

            // Extract and format rejected shipping order items
            List<Map<String, Object>> rejectedItems = extractRejectedItems(variables);
            shippingOrder.put("shippingOrderItem", rejectedItems);

            // Add product order reference
            if (variables.containsKey("productOrder")) {
                shippingOrder.put("productOrder", variables.get("productOrder"));
            }

            // Build the final error structure
            Map<String, Object> event = new HashMap<>();
            event.put("shippingOrder", shippingOrder);

            errorResponse.put("eventTime", eventTime);
            errorResponse.put("eventType", "ShippingOrderStateChangeEvent");
            errorResponse.put("correlationId", correlationId);
            errorResponse.put("event", event);

            logger.info("Error response built successfully for correlationId: {}", correlationId);

            client.newCompleteCommand(job.getKey())
                    .variables(Map.of("CamundaResponse", errorResponse))
                    .send()
                    .join();

        } catch (Exception e) {
            logger.error("Error building error response: {}", e.getMessage(), e);

            // Create a minimal error response if the error handler fails
            errorResponse = createMinimalErrorResponse(getStringValue(variables, "correlationId", "unknown"));
            client.newCompleteCommand(job.getKey())
                    .variables(Map.of("CamundaResponse", errorResponse))
                    .send()
                    .join();
        }

    }

    /**
     * Creates a characteristic object for the shipping order
     */
    private Map<String, Object> createCharacteristic(String name, String value, String valueType) {
        Map<String, Object> characteristic = new HashMap<>();
        characteristic.put("name", name);
        characteristic.put("value", value);
        characteristic.put("valueType", valueType);
        return characteristic;
    }

    /**
     * Extracts and formats rejected items from input variables
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRejectedItems(Map<String, Object> variables) {
        List<Map<String, Object>> rejectedItems = new ArrayList<>();

        try {
            // Get shipping order items from variables
            if (variables.containsKey("shippingOrderItem")) {
                List<Map<String, Object>> inputItems = (List<Map<String, Object>>) variables.get("shippingOrderItem");

                // Process each item
                for (Map<String, Object> item : inputItems) {
                    Map<String, Object> rejectedItem = new HashMap<>();

                    // Always prioritize productOrderItem.productOrderId for the ID
                    String itemId = null;
                    if (item.containsKey("productOrderItem")) {
                        Map<String, Object> productOrderItem = (Map<String, Object>) item.get("productOrderItem");
                        if (productOrderItem != null && productOrderItem.containsKey("productOrderId")) {
                            itemId = productOrderItem.get("productOrderId").toString();
                        }
                    }
                    // Only fallback to item's ID if productOrderItem.productOrderId is not available
                    else if (item.containsKey("id")) {
                        itemId = item.get("id").toString();
                    }

                    // If we found an ID, create a rejected item
                    if (itemId != null) {
                        rejectedItem.put("id", itemId);
                        rejectedItem.put("status", "Rejected");

                        // Use the original action or default to "add"
                        String action = item.containsKey("action") ?
                                item.get("action").toString() : "add";
                        rejectedItem.put("action", action);

                        rejectedItems.add(rejectedItem);
                        logger.debug("Added rejected item with ID: {}", itemId);
                    }
                }
            }

            // If no valid items found, add a placeholder
            if (rejectedItems.isEmpty()) {
                // Try to use product order ID for the placeholder if available
                String placeholderId = "unknown";
                if (variables.containsKey("productOrder")) {
                    Map<String, Object> productOrder = (Map<String, Object>) variables.get("productOrder");
                    if (productOrder != null && productOrder.containsKey("id")) {
                        placeholderId = productOrder.get("id").toString() + "_item";
                    }
                }

                Map<String, Object> placeholder = new HashMap<>();
                placeholder.put("id", placeholderId);
                placeholder.put("status", "Rejected");
                placeholder.put("action", "add");
                rejectedItems.add(placeholder);
                logger.debug("Using placeholder rejected item with ID: {}", placeholderId);
            }
        } catch (Exception e) {
            logger.warn("Error extracting rejected items: {}", e.getMessage());
            // Add a fallback item
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("id", "unknown");
            fallback.put("status", "Rejected");
            fallback.put("action", "add");
            rejectedItems.add(fallback);
        }

        return rejectedItems;
    }

    /**
     * Creates a minimal error response for fallback cases
     */
    private Map<String, Object> createMinimalErrorResponse(String correlationId) {
        Map<String, Object> minimalResponse = new HashMap<>();

        String eventTime = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);

        Map<String, Object> shippingOrder = new HashMap<>();
        shippingOrder.put("status", "Rejected");

        List<Map<String, Object>> characteristics = new ArrayList<>();
        characteristics.add(createCharacteristic("ErrorCode", "CAM-500", "String"));
        characteristics.add(createCharacteristic("ErrorDescription", "An internal error occurred", "String"));
        shippingOrder.put("shippingOrderCharacteristic", characteristics);

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", "unknown");
        item.put("status", "Rejected");
        item.put("action", "add");
        items.add(item);
        shippingOrder.put("shippingOrderItem", items);

        shippingOrder.put("productOrder", Map.of("id", "EB-UNKNOWN"));

        Map<String, Object> event = new HashMap<>();
        event.put("shippingOrder", shippingOrder);

        minimalResponse.put("eventTime", eventTime);
        minimalResponse.put("eventType", "ShippingOrderStateChangeEvent");
        minimalResponse.put("correlationId", correlationId);
        minimalResponse.put("event", event);

        return minimalResponse;
    }

    /**
     * Helper method to safely get a string value from a map with a default fallback
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return defaultValue;
        }
        return map.get(key).toString();
    }
}