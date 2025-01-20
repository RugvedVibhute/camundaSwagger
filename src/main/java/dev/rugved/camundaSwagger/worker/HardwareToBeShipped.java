package dev.rugved.camundaSwagger.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rugved.camundaSwagger.entity.SkuId;
import dev.rugved.camundaSwagger.service.NetworkElementTypeService;
import dev.rugved.camundaSwagger.service.SkuIdService;
import dev.rugved.camundaSwagger.service.UniWithOrWithoutNtuService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HardwareToBeShipped {

    @Autowired
    private NetworkElementTypeService service;

    @Autowired
    private UniWithOrWithoutNtuService uniService;

    @Autowired
    private SkuIdService skuIdService;

    @JobWorker(type = "HardwareToBeShipped")
    public void hardwareToBeShipped(final JobClient client, final ActivatedJob job) throws JsonProcessingException {

        // Fetch the variables from the job
        String var = job.getVariables();
        System.out.println("Job Variables: " + var);

        // Parse JSON using Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(var);

        // Initialize variables
        String networkElement = null;
        String distance = null, ntuRequired = null, ntuSize = null, uniPortCapacity = null, uniInterfaceType = null;

        // Navigate to "networkElement" value
        JsonNode shippingOrderItems = rootNode.path("shippingOrderItem");
        if (shippingOrderItems.isArray()) {
            for (JsonNode item : shippingOrderItems) {
                JsonNode shipmentItems = item.path("shipment").path("shipmentItem");

                if (shipmentItems.isArray()) {
                    for (JsonNode shipmentItem : shipmentItems) {
                        JsonNode productCharacteristics = shipmentItem
                                .path("product")
                                .path("productCharacteristic");

                        // Process each characteristic and collect values
                        for (JsonNode characteristic : productCharacteristics) {
                            String name = characteristic.path("name").asText();
                            String value = characteristic.path("value").asText();

                            switch (name) {
                                case "networkElement":
                                    networkElement = value;
                                    break;
                                case "distance":
                                    distance = value;
                                    break;
                                case "NTURequired":
                                    ntuRequired = value;
                                    break;
                                case "ntuSize":
                                    ntuSize = value;
                                    break;
                                case "UNIPortCapacity":
                                    uniPortCapacity = value;
                                    break;
                                case "InterfaceType":
                                    uniInterfaceType = value;
                                    break;
                            }
                        }
                    }
                }
            }
        }

        // Ensure that networkElement is found before proceeding
        if (networkElement == null) {
            System.out.println("Network Element not found, unable to determine vendorType.");
            return;
        }

        // Fetch vendorType using the networkElement
        String vendorType = service.getVendorType(networkElement);
        System.out.println("Vendor Type: " + vendorType);

        if ("No".equalsIgnoreCase(ntuRequired)) {
            ntuSize = "0";
        }

        // Ensure that all required values are gathered before proceeding
        if (distance != null && ntuRequired != null && ntuSize != null && uniPortCapacity != null && uniInterfaceType != null) {

            // Map the distance to database-compatible ranges
            String distanceRanges = mapDistanceToDatabaseValue(distance);
            System.out.println("Mapped Distance Range: " + distanceRanges);

            // Call the service to get aaUniSfp once after all necessary values are collected
            String aaUniSfp = uniService.getAaUniSfp(distanceRanges, ntuRequired, ntuSize, vendorType, uniPortCapacity, uniInterfaceType);
            System.out.println("aaUniSfp: " + aaUniSfp);

            // Fetch SKU ID using aaUniSfp
            String skuIdValue = null;
            if (aaUniSfp != null) {
                SkuId skuId = skuIdService.getSkuIdByAaUniSfp(aaUniSfp);
                if (skuId != null) {
                    skuIdValue = skuId.getAaUniSfpSkuId();
                    System.out.println("SKU ID: " + skuIdValue);
                } else {
                    System.out.println("No SKU ID found for aaUniSfp: " + aaUniSfp);
                }
            }

            // Create output variables
            Map<String, Object> output = new HashMap<>();
            output.put("aaUniSfp", aaUniSfp);
            output.put("skuId", skuIdValue);

            // Complete the job
            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            System.out.println("Job completed with variables: " + output);
        } else {
            System.out.println("Missing required inputs for fetching aaUniSfp.");
        }
    }

    private String mapDistanceToDatabaseValue(String distance) {
        try {
            // Convert distance input to a numeric value
            int numericDistance = Integer.parseInt(distance.trim());

            // Map input distance to appropriate database conditions
            if (numericDistance < 100) {
                return "distance_ranges = '<100' OR distance_ranges = '< 300' OR distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance >= 100 && numericDistance < 300) {
                return "distance_ranges = '< 300' OR distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance >= 300 && numericDistance < 500) {
                return "distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance >= 500 && numericDistance < 10000) {
                return "distance_ranges = '<10000'";
            } else if (numericDistance >= 10000 && numericDistance < 40000) {
                return "distance_ranges = '>=10000 and <40000'";
            } else if (numericDistance >= 40000 && numericDistance < 80000) {
                return "distance_ranges = '>=40000 and <80000'";
            } else {
                throw new IllegalArgumentException("Distance out of range: " + distance);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid distance input: " + distance, e);
        }
    }
}