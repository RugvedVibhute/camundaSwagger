package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.entity.NtuType;
import dev.rugved.camundaSwagger.entity.SkuId;
import dev.rugved.camundaSwagger.service.*;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Component
public class HardwareToBeShipped {

    private static final Logger logger = LoggerFactory.getLogger(HardwareToBeShipped.class);

    private final NetworkElementTypeService service;
    private final UniWithOrWithoutNtuService uniService;
    private final SkuIdService skuIdService;
    private final NtuNniSfpOrAaSfpService ntuNniSfpOrAaSfpService;
    private final NtuTypeService ntuTypeService;
    private final ErrorHandlerService errorHandlerService;

    public HardwareToBeShipped(NetworkElementTypeService service, UniWithOrWithoutNtuService uniService,
                               SkuIdService skuIdService, NtuNniSfpOrAaSfpService ntuNniSfpOrAaSfpService,
                               NtuTypeService ntuTypeService, ErrorHandlerService errorHandlerService) {
        this.service = service;
        this.uniService = uniService;
        this.skuIdService = skuIdService;
        this.ntuNniSfpOrAaSfpService = ntuNniSfpOrAaSfpService;
        this.ntuTypeService = ntuTypeService;
        this.errorHandlerService = errorHandlerService;
    }

    @JobWorker(type = JOB_TYPE_HARDWARE_TO_BE_SHIPPED)
    public void hardwareToBeShipped(final JobClient client, final ActivatedJob job) {
        Map<String, Object> output = new HashMap<>();

        try {
            String networkElement = job.getVariable(NETWORK_ELEMENT).toString();
            String distance = job.getVariable(DISTANCE).toString();
            String ntuRequired = job.getVariable(NTU_REQUIRED).toString();
            String ntuSize = job.getVariable(NTU_SIZE).toString();
            String uniPortCapacity = job.getVariable(UNI_PORT_CAPACITY).toString();
            String uniInterfaceType = job.getVariable(UNI_INTERFACE_TYPE).toString();

            logger.info("Processing HardwareToBeShipped job | networkElement: {}, distance: {}, ntuRequired: {}",
                    networkElement, distance, ntuRequired);

            if ("No".equalsIgnoreCase(ntuRequired)) {
                handleNoNtuCase(output, networkElement, distance, uniPortCapacity, uniInterfaceType, ntuRequired);
            } else if ("Yes".equalsIgnoreCase(ntuRequired)) {
                handleYesNtuCase(output, networkElement, distance, ntuSize, uniPortCapacity, uniInterfaceType, ntuRequired);
            }

            output.put(ERROR_MESSAGE, null);
            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            logger.info("Job completed successfully with variables: {}", output);

        } catch (Exception e) {
            output = errorHandlerService.handleError(e, JOB_TYPE_HARDWARE_TO_BE_SHIPPED);
            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        }
    }

    private void handleNoNtuCase(Map<String, Object> output, String networkElement, String distance,
                                 String uniPortCapacity, String uniInterfaceType, String ntuRequired) {
        String distanceRanges = mapDistanceToDatabaseValue(distance);
        String vendorType = (networkElement != null) ? service.getVendorType(networkElement) : null;

        if (vendorType == null) {
            throw new IllegalArgumentException("No Vendor Type found for networkElement: " + networkElement);
        }

        String aaUniSfp = uniService.getAaUniSfp(distanceRanges, ntuRequired, "0", vendorType, uniPortCapacity, uniInterfaceType);
        if (aaUniSfp == null) {
            throw new IllegalArgumentException("No aaUniSfp found for " + distanceRanges +
                    ", vendorType: " + vendorType + ", uniPortCapacity: " + uniPortCapacity + ", uniInterfaceType: " + uniInterfaceType);
        }

        SkuId skuId = skuIdService.getSkuIdByAaUniSfp(aaUniSfp);
        String skuIdValue = (skuId != null) ? skuId.getAaUniSfpSkuId() : null;
        if (skuIdValue == null) {
            throw new IllegalArgumentException("No skuId found for aaUniSfp: " + aaUniSfp);
        }

        output.put(AA_UNI_SFP, aaUniSfp);
        output.put(SKU_ID, skuIdValue);
        output.put(NTU_REQUIRED, ntuRequired);
    }

    private void handleYesNtuCase(Map<String, Object> output, String networkElement, String distance,
                                  String ntuSize, String uniPortCapacity, String uniInterfaceType, String ntuRequired) {
        String vendorType = service.getVendorType(networkElement);
        String distanceRanges = mapDistanceToDatabaseValue(distance);

        NtuType ntuType = ntuTypeService.getNtuTypeBySize(ntuSize);
        if (ntuType == null) {
            throw new IllegalArgumentException("No NTU Type found for size: " + ntuSize);
        }

        String ntuTypeSkuIdValue = skuIdService.getSkuIdByAaUniSfp(ntuType.getNtuType()).getAaUniSfpSkuId();
        if (ntuTypeSkuIdValue == null) {
            throw new IllegalArgumentException("No SKU ID found for NTU Type: " + ntuType.getNtuType());
        }

        String ntuNniSfp = ntuNniSfpOrAaSfpService.getNtuNniSfp(ntuSize, distanceRanges, vendorType);
        if (ntuNniSfp == null) {
            throw new IllegalArgumentException("No NTU NNI SFP found for NTU Size: " + ntuSize + distanceRanges + "Vendor Type" + vendorType);
        }
        String ntuNniSfpSkuIdValue = Optional.ofNullable(skuIdService.getSkuIdByAaUniSfp(ntuNniSfp))
                .map(SkuId::getAaUniSfpSkuId)
                .orElseThrow(() -> new IllegalArgumentException("No SkuId found for NTU NNI SFP: " + ntuNniSfp));


        String aaSfp = ntuNniSfpOrAaSfpService.getAaSfp(ntuSize, distanceRanges, vendorType);
        if (aaSfp == null) {
            throw new IllegalArgumentException("No AA SFP found for NTU Size: " + ntuSize + ", Distance Ranges: " + distanceRanges + ", Vendor Type: " + vendorType);
        }
        String aaSfpSkuIdValue = Optional.ofNullable(skuIdService.getSkuIdByAaUniSfp(aaSfp))
                .map(SkuId::getAaUniSfpSkuId)
                .orElseThrow(() -> new IllegalArgumentException("No SkuId found for AA SFP: " + aaSfp));

        String aaUniSfp = uniService.getAaUniSfp(distanceRanges, ntuRequired, ntuSize, vendorType, uniPortCapacity, uniInterfaceType);
        if (aaUniSfp == null) {
            throw new IllegalArgumentException("No AA UNI SFP found for given parameters.");
        }

        String skuIdValue = Optional.ofNullable(skuIdService.getSkuIdByAaUniSfp(aaUniSfp))
                .map(SkuId::getAaUniSfpSkuId)
                .orElseThrow(() -> new IllegalArgumentException("No SkuId found for AA UNI SFP: " + aaUniSfp));
        output.put(NTU_TYPE, ntuType.getNtuType());
        output.put(NTU_TYPE_SKU_ID, ntuTypeSkuIdValue);
        output.put(NTU_NNI_SFP, ntuNniSfp);
        output.put(NTU_NNI_SFP_SKU_ID, ntuNniSfpSkuIdValue);
        output.put(AA_SFP, aaSfp);
        output.put(AA_SFP_SKU_ID, aaSfpSkuIdValue);
        output.put(AA_UNI_SFP, aaUniSfp);
        output.put(SKU_ID, skuIdValue);
        output.put(NTU_REQUIRED, ntuRequired);
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
            } else if (numericDistance >= 10000 && numericDistance < 40000) {
                return "distance_ranges = '>=10000 and <40000'";
            } else if (numericDistance >= 40000 && numericDistance < 80000) {
                return "distance_ranges = '>=40000 and <80000'";
            } else if (numericDistance < 10000) {
                return "distance_ranges = '<10000'";
            } else {
                return "distance_ranges = 'incorrect distance value'";
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid distance value: " + distance);
            return null;
        }
    }

}
