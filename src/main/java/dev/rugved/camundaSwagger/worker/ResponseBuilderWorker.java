package dev.rugved.camundaSwagger.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Worker responsible for building the final response by combining the input data
 * with all the details fetched from the database workers.
 */
@Component
public class ResponseBuilderWorker {

    private static final Logger logger = LoggerFactory.getLogger(ResponseBuilderWorker.class);

    // Product specification IDs
    private static final String TARGET_PRODUCT_SPEC_ID = "601c8c38-07c6-4deb-b473-f15cd843b712";
    private static final String NTU_PRODUCT_SPEC_ID = "84b0d8a2-1b90-47ab-b8d8-f119ea330bef";

    @JobWorker(type = "buildResponse")
    public void buildFinalResponse(final JobClient client, final ActivatedJob job) {
        logger.info("Starting to build final response - jobKey: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        Map<String, Object> finalResponse = new HashMap<>();

        try {
            // Check for error message
            String errorMessage = getStringValue(variables, "errorMessage");
            if (errorMessage != null) {
                logger.warn("Error detected while building response: {}", errorMessage);
                finalResponse.put("message", "Data is not available in lookup tables");
                finalResponse.put("error", errorMessage);
                client.newCompleteCommand(job.getKey())
                        .variables(Map.of("CamundaResponse", finalResponse))
                        .send()
                        .join();
                return;
            }

            // Build the base response from original input
            buildBaseResponse(variables, finalResponse);

            // Add WBSHeader information from fetchWBSDetails worker
            addWbsHeaderInfo(variables, finalResponse);

            // Add address information from shipToAddress worker
            addAddressInfo(variables, finalResponse);

            // Add NTU and UNI hardware information from HardwareToBeShipped worker
            addHardwareInfo(variables, finalResponse);

            logger.info("Successfully built final response - jobKey: {}", job.getKey());

            // Complete the job with the final response
            client.newCompleteCommand(job.getKey())
                    .variables(Map.of("CamundaResponse", finalResponse))
                    .send()
                    .join();

        } catch (Exception e) {
            logger.error("Error building final response: {}", e.getMessage(), e);

            finalResponse.put("message", "Error building response");
            finalResponse.put("error", e.getMessage());

            client.newCompleteCommand(job.getKey())
                    .variables(Map.of("CamundaResponse", finalResponse))
                    .send()
                    .join();
        }
    }

    /**
     * Builds the base response structure from the original input
     */
    @SuppressWarnings("unchecked")
    private void buildBaseResponse(Map<String, Object> variables, Map<String, Object> finalResponse) {
        // Copy placeTo information
        if (variables.containsKey("placeTo")) {
            finalResponse.put("placeTo", variables.get("placeTo"));
        }

        // Copy base relatedParty information (will be extended later)
        if (variables.containsKey("relatedParty")) {
            List<Map<String, Object>> relatedParty = (List<Map<String, Object>>) variables.get("relatedParty");
            finalResponse.put("relatedParty", new ArrayList<>(relatedParty));
        } else {
            finalResponse.put("relatedParty", new ArrayList<>());
        }

        // Copy base shippingOrderCharacteristic information (will be extended later)
        if (variables.containsKey("shippingOrderCharacteristic")) {
            List<Map<String, Object>> characteristics =
                    (List<Map<String, Object>>) variables.get("shippingOrderCharacteristic");
            finalResponse.put("shippingOrderCharacteristic", new ArrayList<>(characteristics));
        } else {
            finalResponse.put("shippingOrderCharacteristic", new ArrayList<>());
        }

        // Copy shippingOrderItem information (will be modified later)
        if (variables.containsKey("shippingOrderItem")) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) variables.get("shippingOrderItem");
            finalResponse.put("shippingOrderItem", deepCopy(items));
        } else {
            finalResponse.put("shippingOrderItem", new ArrayList<>());
        }

        // Copy productOrder information
        if (variables.containsKey("productOrder")) {
            finalResponse.put("productOrder", variables.get("productOrder"));
        }

        // Copy any other fields that might be important
        copyInputFieldIfPresent(variables, finalResponse, "productOrder");
        copyInputFieldIfPresent(variables, finalResponse, "description");
        copyInputFieldIfPresent(variables, finalResponse, "shippingDate");
        copyInputFieldIfPresent(variables, finalResponse, "requestedDate");
        copyInputFieldIfPresent(variables, finalResponse, "id");
        copyInputFieldIfPresent(variables, finalResponse, "href");
        copyInputFieldIfPresent(variables, finalResponse, "@type");
        // Add other fields as needed
    }

    /**
     * Helper method to copy a field from input to output if it exists
     */
    private void copyInputFieldIfPresent(Map<String, Object> source, Map<String, Object> target, String fieldName) {
        if (source.containsKey(fieldName) && !target.containsKey(fieldName)) {
            target.put(fieldName, source.get(fieldName));
        }
    }

    /**
     * Adds WBS header information to the response
     */
    @SuppressWarnings("unchecked")
    private void addWbsHeaderInfo(Map<String, Object> variables, Map<String, Object> finalResponse) {
        String wbsHeader = getStringValue(variables, "wbsHeader");
        String stateOrProvince = getStringValue(variables, "stateOrProvince");

        if (wbsHeader != null) {
            Map<String, Object> wbsCharacteristic = new HashMap<>();
            wbsCharacteristic.put("name", "WBSHeader");
            wbsCharacteristic.put("value", wbsHeader);
            wbsCharacteristic.put("valueType", "String");

            List<Map<String, Object>> characteristics =
                    (List<Map<String, Object>>) finalResponse.get("shippingOrderCharacteristic");

            // Check if WBSHeader already exists
            boolean wbsHeaderExists = false;
            for (Map<String, Object> characteristic : characteristics) {
                if ("WBSHeader".equals(getStringValue(characteristic, "name"))) {
                    wbsHeaderExists = true;
                    break;
                }
            }

            if (!wbsHeaderExists) {
                characteristics.add(wbsCharacteristic);
            }
        }
    }

    /**
     * Adds address information from the shipToAddress worker
     */
    @SuppressWarnings("unchecked")
    private void addAddressInfo(Map<String, Object> variables, Map<String, Object> finalResponse) {
        List<Map<String, Object>> relatedParty = (List<Map<String, Object>>) finalResponse.get("relatedParty");

        // Add shipToAddress
        if (variables.containsKey("shipToAddresses")) {
            List<Map<String, Object>> shipToAddresses = (List<Map<String, Object>>) variables.get("shipToAddresses");
            if (!shipToAddresses.isEmpty()) {
                Map<String, Object> shipToAddress = shipToAddresses.get(0);

                // Check if shipToAddress already exists
                boolean shipToExists = false;
                for (Map<String, Object> party : relatedParty) {
                    if ("shipToAddress".equals(getStringValue(party, "@referredType"))) {
                        shipToExists = true;
                        break;
                    }
                }

                if (!shipToExists) {
                    Map<String, Object> addressParty = new HashMap<>();
                    addressParty.put("id", shipToAddress.get("shipToAddressId"));
                    addressParty.put("role", shipToAddress.get("shipToAddressRole"));
                    addressParty.put("@referredType", "shipToAddress");

                    relatedParty.add(addressParty);
                }
            }
        }

        // Add other addresses (soldTo, networkSite, additionalPartner)
        if (variables.containsKey("otherAddresses")) {
            List<Map<String, Object>> otherAddresses = (List<Map<String, Object>>) variables.get("otherAddresses");
            if (!otherAddresses.isEmpty()) {
                Map<String, Object> otherAddress = otherAddresses.get(0);

                // Add soldToAddress
                addAddressIfNotExists(relatedParty, otherAddress, "soldToAddressId", "soldToAddressRole", "soldToAddress");

                // Add networkSiteAddress
                addAddressIfNotExists(relatedParty, otherAddress, "networkSiteAddressId", "networkSiteAddressRole", "networkSiteAddress");

                // Add additionalPartnerAddress
                addAddressIfNotExists(relatedParty, otherAddress, "additionalPartnerAddressId", "additionalPartnerAddressRole", "additionalPartnerAddress");
            }
        }
    }

    /**
     * Helper method to add an address to related party if it doesn't already exist
     */
    private void addAddressIfNotExists(List<Map<String, Object>> relatedParty,
                                       Map<String, Object> otherAddress,
                                       String idField,
                                       String roleField,
                                       String referredType) {
        if (otherAddress.containsKey(idField) && otherAddress.containsKey(roleField)) {
            boolean addressExists = false;
            for (Map<String, Object> party : relatedParty) {
                if (referredType.equals(getStringValue(party, "@referredType"))) {
                    addressExists = true;
                    break;
                }
            }

            if (!addressExists) {
                Map<String, Object> addressParty = new HashMap<>();
                addressParty.put("id", otherAddress.get(idField));
                addressParty.put("role", otherAddress.get(roleField));
                addressParty.put("@referredType", referredType);
                relatedParty.add(addressParty);
                logger.debug("Added {} party to relatedParty with id: {}", referredType, otherAddress.get(idField));
            }
        }
    }

    /**
     * Adds hardware information from the HardwareToBeShipped worker
     */
    @SuppressWarnings("unchecked")
    private void addHardwareInfo(Map<String, Object> variables, Map<String, Object> finalResponse) {
        String ntuRequired = getStringValue(variables, "ntuRequired");
        List<Map<String, Object>> shippingOrderItems = (List<Map<String, Object>>) finalResponse.get("shippingOrderItem");

        logger.debug("Processing hardware info with ntuRequired={}", ntuRequired);

        if ("Yes".equalsIgnoreCase(ntuRequired)) {
            addYesNtuHardwareInfo(variables, shippingOrderItems);
        } else if ("No".equalsIgnoreCase(ntuRequired)) {
            addNoNtuHardwareInfo(variables, shippingOrderItems);
        }
    }

    /**
     * Adds hardware information for the "Yes NTU" case
     */
    @SuppressWarnings("unchecked")
    private void addYesNtuHardwareInfo(Map<String, Object> variables, List<Map<String, Object>> shippingOrderItems) {
        // Get hardware details
        String ntuType = getStringValue(variables, "ntuType");
        String ntuNniSfp = getStringValue(variables, "ntuNniSfp");
        String aaSfp = getStringValue(variables, "aaSfp");
        String ntuTypeSkuId = getStringValue(variables, "ntuTypeSkuId");
        String ntuNniSfpSkuId = getStringValue(variables, "ntuNniSfpSkuId");
        String aaSfpSkuId = getStringValue(variables, "aaSfpSkuId");
        String aaUniSfp = getStringValue(variables, "aaUniSfp");
        String skuId = getStringValue(variables, "skuId");

        logger.debug("Processing Yes NTU hardware info - found values: ntuType={}, ntuNniSfp={}, aaSfp={}, aaUniSfp={}",
                ntuType != null, ntuNniSfp != null, aaSfp != null, aaUniSfp != null);

        // Process each shipping order item
        for (Map<String, Object> item : shippingOrderItems) {
            Map<String, Object> shipment = (Map<String, Object>) item.get("shipment");
            if (shipment != null) {
                List<Map<String, Object>> shipmentItems = (List<Map<String, Object>>) shipment.get("shipmentItem");
                if (shipmentItems != null && !shipmentItems.isEmpty()) {
                    for (Map<String, Object> shipmentItem : shipmentItems) {
                        Map<String, Object> product = (Map<String, Object>) shipmentItem.get("product");
                        if (product != null) {
                            Map<String, Object> productSpec = (Map<String, Object>) product.get("productSpecification");
                            if (productSpec != null) {
                                String productSpecId = getStringValue(productSpec, "id");

                                // Add NTU-related hardware info to the NTU product
                                if (NTU_PRODUCT_SPEC_ID.equals(productSpecId)) {
                                    addNtuHardwareInfo(product, ntuType, ntuNniSfp, aaSfp,
                                            ntuTypeSkuId, ntuNniSfpSkuId, aaSfpSkuId);
                                    logger.debug("Added NTU hardware info to NTU product");
                                }

                                // Add UNI-related hardware info to the UNI product
                                else if (TARGET_PRODUCT_SPEC_ID.equals(productSpecId)) {
                                    addUniHardwareInfo(product, aaUniSfp, skuId, true);
                                    logger.debug("Added UNI hardware info to UNI product (Yes NTU case)");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds hardware information for the "No NTU" case
     */
    @SuppressWarnings("unchecked")
    private void addNoNtuHardwareInfo(Map<String, Object> variables, List<Map<String, Object>> shippingOrderItems) {
        // Get hardware details
        String aaUniSfp = getStringValue(variables, "aaUniSfp");
        String skuId = getStringValue(variables, "skuId");

        logger.debug("Processing No NTU hardware info - found values: aaUniSfp={}, skuId={}",
                aaUniSfp != null, skuId != null);

        // We only add UNI-related info in this case
        for (Map<String, Object> item : shippingOrderItems) {
            Map<String, Object> shipment = (Map<String, Object>) item.get("shipment");
            if (shipment != null) {
                List<Map<String, Object>> shipmentItems = (List<Map<String, Object>>) shipment.get("shipmentItem");
                if (shipmentItems != null && !shipmentItems.isEmpty()) {
                    for (Map<String, Object> shipmentItem : shipmentItems) {
                        Map<String, Object> product = (Map<String, Object>) shipmentItem.get("product");
                        if (product != null) {
                            Map<String, Object> productSpec = (Map<String, Object>) product.get("productSpecification");
                            if (productSpec != null) {
                                String productSpecId = getStringValue(productSpec, "id");

                                // Add UNI-related hardware info to the UNI product
                                if (TARGET_PRODUCT_SPEC_ID.equals(productSpecId)) {
                                    addUniHardwareInfo(product, aaUniSfp, skuId, false);
                                    logger.debug("Added UNI hardware info to UNI product (No NTU case)");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds NTU-related hardware info to the product characteristics
     */
    @SuppressWarnings("unchecked")
    private void addNtuHardwareInfo(Map<String, Object> product,
                                    String ntuType, String ntuNniSfp, String aaSfp,
                                    String ntuTypeSkuId, String ntuNniSfpSkuId, String aaSfpSkuId) {
        List<Map<String, Object>> characteristics =
                (List<Map<String, Object>>) product.get("productCharacteristic");

        // Add NTU Type - check if it already exists first
        boolean ntuTypeExists = false;
        boolean ntuNniSfpExists = false;
        boolean aaSfpExists = false;
        boolean ntuTypeSkuIdExists = false;
        boolean ntuNniSfpSkuIdExists = false;
        boolean aaSfpSkuIdExists = false;

        for (Map<String, Object> characteristic : characteristics) {
            String name = getStringValue(characteristic, "name");
            if ("NTU Type".equals(name)) ntuTypeExists = true;
            else if ("NTU NNI SFP".equals(name)) ntuNniSfpExists = true;
            else if ("AA SFP".equals(name)) aaSfpExists = true;
            else if ("NTU Type SKU ID".equals(name)) ntuTypeSkuIdExists = true;
            else if ("NTU NNI SFP SKU ID".equals(name)) ntuNniSfpSkuIdExists = true;
            else if ("AA SFP SKU ID".equals(name)) aaSfpSkuIdExists = true;
        }

        // Add characteristics that don't already exist
        if (!ntuTypeExists && ntuType != null) {
            addCharacteristic(characteristics, "NTU Type", ntuType);
            logger.debug("Added NTU Type characteristic: {}", ntuType);
        }

        if (!ntuNniSfpExists && ntuNniSfp != null) {
            addCharacteristic(characteristics, "NTU NNI SFP", ntuNniSfp);
            logger.debug("Added NTU NNI SFP characteristic: {}", ntuNniSfp);
        }

        if (!aaSfpExists && aaSfp != null) {
            addCharacteristic(characteristics, "AA SFP", aaSfp);
            logger.debug("Added AA SFP characteristic: {}", aaSfp);
        }

        if (!ntuTypeSkuIdExists && ntuTypeSkuId != null) {
            addCharacteristic(characteristics, "NTU Type SKU ID", ntuTypeSkuId);
            logger.debug("Added NTU Type SKU ID characteristic: {}", ntuTypeSkuId);
        }

        if (!ntuNniSfpSkuIdExists && ntuNniSfpSkuId != null) {
            addCharacteristic(characteristics, "NTU NNI SFP SKU ID", ntuNniSfpSkuId);
            logger.debug("Added NTU NNI SFP SKU ID characteristic: {}", ntuNniSfpSkuId);
        }

        if (!aaSfpSkuIdExists && aaSfpSkuId != null) {
            addCharacteristic(characteristics, "AA SFP SKU ID", aaSfpSkuId);
            logger.debug("Added AA SFP SKU ID characteristic: {}", aaSfpSkuId);
        }
    }

    /**
     * Adds UNI-related hardware info to the product characteristics
     */
    @SuppressWarnings("unchecked")
    private void addUniHardwareInfo(Map<String, Object> product, String aaUniSfp, String skuId, boolean isNtuRequired) {
        List<Map<String, Object>> characteristics =
                (List<Map<String, Object>>) product.get("productCharacteristic");

        // Choose appropriate characteristic names based on whether NTU is required
        String sfpName = isNtuRequired ? "UNI SFP" : "AA UNI SFP";
        String skuIdName = isNtuRequired ? "UNI SFP SKU ID" : "AA UNI SFP SKU ID";

        logger.debug("UNI hardware info - isNtuRequired={}, using names: {}, {}",
                isNtuRequired, sfpName, skuIdName);

        // Check if characteristics already exist
        boolean sfpExists = false;
        boolean skuIdExists = false;

        for (Map<String, Object> characteristic : characteristics) {
            String name = getStringValue(characteristic, "name");
            if (sfpName.equals(name)) sfpExists = true;
            else if (skuIdName.equals(name)) skuIdExists = true;
        }

        // Add characteristics that don't already exist
        if (!sfpExists && aaUniSfp != null) {
            addCharacteristic(characteristics, sfpName, aaUniSfp);
            logger.debug("Added {} characteristic: {}", sfpName, aaUniSfp);
        }

        if (!skuIdExists && skuId != null) {
            addCharacteristic(characteristics, skuIdName, skuId);
            logger.debug("Added {} characteristic: {}", skuIdName, skuId);
        }
    }

    /**
     * Adds a new characteristic to the list of characteristics
     */
    private void addCharacteristic(List<Map<String, Object>> characteristics, String name, String value) {
        Map<String, Object> characteristic = new HashMap<>();
        characteristic.put("name", name);
        characteristic.put("value", value);
        characteristic.put("valueType", "String");
        characteristics.add(characteristic);
    }

    /**
     * Creates a deep copy of a list of maps (for nested objects)
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> deepCopy(List<Map<String, Object>> original) {
        if (original == null) {
            return null;
        }

        List<Map<String, Object>> copy = new ArrayList<>();
        for (Map<String, Object> item : original) {
            Map<String, Object> itemCopy = new HashMap<>();
            for (Map.Entry<String, Object> entry : item.entrySet()) {
                if (entry.getValue() instanceof List) {
                    if (entry.getValue() instanceof List<?> &&
                            !((List<?>) entry.getValue()).isEmpty() &&
                            ((List<?>) entry.getValue()).get(0) instanceof Map) {
                        itemCopy.put(entry.getKey(), deepCopy((List<Map<String, Object>>) entry.getValue()));
                    } else {
                        itemCopy.put(entry.getKey(), new ArrayList<>((List<?>) entry.getValue()));
                    }
                } else if (entry.getValue() instanceof Map) {
                    itemCopy.put(entry.getKey(), new HashMap<>((Map<?, ?>) entry.getValue()));
                } else {
                    itemCopy.put(entry.getKey(), entry.getValue());
                }
            }
            copy.add(itemCopy);
        }
        return copy;
    }

    /**
     * Helper method to safely get a string value from a map
     */
    private String getStringValue(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return null;
        }
        return map.get(key).toString();
    }
}