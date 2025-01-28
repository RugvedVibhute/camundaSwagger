package dev.rugved.camundaSwagger.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rugved.camundaSwagger.entity.NtuType;
import dev.rugved.camundaSwagger.entity.SkuId;
import dev.rugved.camundaSwagger.service.*;
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

    @Autowired
    private NtuNniSfpOrAaSfpService ntuNniSfpOrAaSfpService;

    @Autowired
    private NtuTypeService ntuTypeService;

    @JobWorker(type = "HardwareToBeShipped", tenantIds = "Infosys")
    public void hardwareToBeShipped(final JobClient client, final ActivatedJob job) throws JsonProcessingException {

        // Fetch the variables from the job
        String var = job.getVariables();
        System.out.println("Job Variables: " + var);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(var);

        // Initialize variables
        String networkElement = null;
        String distance = null, ntuRequired = null, ntuSize = null, uniPortCapacity = null, uniInterfaceType = null;

        // Extract variables from JSON
        JsonNode shippingOrderItems = rootNode.path("shippingOrderItem");
        if (shippingOrderItems.isArray()) {
            for (JsonNode item : shippingOrderItems) {
                JsonNode shipmentItems = item.path("shipment").path("shipmentItem");

                if (shipmentItems.isArray()) {
                    for (JsonNode shipmentItem : shipmentItems) {
                        JsonNode productCharacteristics = shipmentItem
                                .path("product")
                                .path("productCharacteristic");

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

        Map<String, Object> output = new HashMap<>();
        boolean hydration = false; // Default to false unless data is retrieved

        if ("No".equalsIgnoreCase(ntuRequired)) {
            String distanceRanges = mapDistanceToDatabaseValue(distance);
            String vendorType = networkElement != null ? service.getVendorType(networkElement) : null;

            String aaUniSfp = uniService.getAaUniSfp(distanceRanges, ntuRequired, "0", vendorType, uniPortCapacity, uniInterfaceType);
            String skuIdValue = null;
            if (aaUniSfp != null) {
                SkuId skuId = skuIdService.getSkuIdByAaUniSfp(aaUniSfp);
                skuIdValue = skuId != null ? skuId.getAaUniSfpSkuId() : null;
                hydration = true; // Data retrieved successfully
            }

            output.put("aaUniSfp", aaUniSfp);
            output.put("skuId", skuIdValue);
            output.put("ntuRequired", ntuRequired);

        } else if ("Yes".equalsIgnoreCase(ntuRequired)) {
            String ntuTypeValue = null, ntuTypeSkuIdValue = null, ntuNniSfp = null;
            String ntuNniSfpSkuIdValue = null, aaSfp = null, aaSfpSkuIdValue = null, aaUniSfp = null, skuIdValue = null;

            if (ntuSize != null) {
                try {
                    NtuType ntuType = ntuTypeService.getNtuTypeBySize(ntuSize);
                    ntuTypeValue = ntuType.getNtuType();

                    SkuId ntuTypeSkuId = skuIdService.getSkuIdByAaUniSfp(ntuTypeValue);
                    ntuTypeSkuIdValue = ntuTypeSkuId != null ? ntuTypeSkuId.getAaUniSfpSkuId() : null;

                    hydration = true; // Data retrieved successfully
                } catch (IllegalArgumentException e) {
                    System.out.println("Error fetching NTU Type or SKU ID: " + e.getMessage());
                }
            }

            String vendorType = service.getVendorType(networkElement);

            if (distance != null && ntuSize != null && uniPortCapacity != null && uniInterfaceType != null) {
                String distanceRanges = mapDistanceToDatabaseValue(distance);

                ntuNniSfp = ntuNniSfpOrAaSfpService.getNtuNniSfp(ntuSize, distanceRanges, vendorType);

                SkuId ntuNniSfpSkuId = skuIdService.getSkuIdByAaUniSfp(ntuNniSfp);
                ntuNniSfpSkuIdValue = ntuNniSfpSkuId != null ? ntuNniSfpSkuId.getAaUniSfpSkuId() : null;

                aaSfp = ntuNniSfpOrAaSfpService.getAaSfp(ntuSize, distanceRanges, vendorType);

                SkuId aaSfpSkuId = skuIdService.getSkuIdByAaUniSfp(aaSfp);
                aaSfpSkuIdValue = aaSfpSkuId != null ? aaSfpSkuId.getAaUniSfpSkuId() : null;

                aaUniSfp = uniService.getAaUniSfp(distanceRanges, ntuRequired, ntuSize, vendorType, uniPortCapacity, uniInterfaceType);

                if (aaUniSfp != null) {
                    SkuId skuId = skuIdService.getSkuIdByAaUniSfp(aaUniSfp);
                    skuIdValue = skuId != null ? skuId.getAaUniSfpSkuId() : null;
                }

                hydration = true; // Data retrieved successfully
            }

            output.put("ntuType", ntuTypeValue);
            output.put("ntuTypeSkuId", ntuTypeSkuIdValue);
            output.put("ntuNniSfp", ntuNniSfp);
            output.put("ntuNniSfpSkuId", ntuNniSfpSkuIdValue);
            output.put("aaSfp", aaSfp);
            output.put("aaSfpSkuId", aaSfpSkuIdValue);
            output.put("aaUniSfp", aaUniSfp);
            output.put("skuId", skuIdValue);
            output.put("ntuRequired", ntuRequired);
        }

        output.put("hydration", hydration);
        client.newCompleteCommand(job.getKey()).variables(output).send().join();
        System.out.println("Job completed with variables: " + output);
    }

    private String mapDistanceToDatabaseValue(String distance) {
        try {
            int numericDistance = Integer.parseInt(distance.trim());

            if (numericDistance < 100) {
                return "distance_ranges = '<100' OR distance_ranges = '< 300' OR distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance >= 100 && numericDistance < 300) {
                return "distance_ranges = '< 300' OR distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance >= 300 && numericDistance < 500) {
                return "distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else {
                return "distance_ranges = '<10000'";
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid distance value: " + distance);
            return null;
        }
    }
}
