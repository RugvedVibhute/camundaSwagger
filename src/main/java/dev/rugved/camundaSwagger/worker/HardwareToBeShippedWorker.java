package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.entity.NtuType;
import dev.rugved.camundaSwagger.entity.SkuId;
import dev.rugved.camundaSwagger.service.*;
import dev.rugved.camundaSwagger.util.LoggingUtil;
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
public class HardwareToBeShippedWorker {

    private static final Logger logger = LoggerFactory.getLogger(HardwareToBeShippedWorker.class);

    private final NetworkElementTypeService networkElementService;
    private final UniWithOrWithoutNtuService uniService;
    private final SkuIdService skuIdService;
    private final NtuNniSfpOrAaSfpService ntuNniSfpOrAaSfpService;
    private final NtuTypeService ntuTypeService;
    private final ErrorHandlerService errorHandlerService;

    public HardwareToBeShippedWorker(NetworkElementTypeService networkElementService,
                                     UniWithOrWithoutNtuService uniService,
                                     SkuIdService skuIdService,
                                     NtuNniSfpOrAaSfpService ntuNniSfpOrAaSfpService,
                                     NtuTypeService ntuTypeService,
                                     ErrorHandlerService errorHandlerService) {
        this.networkElementService = networkElementService;
        this.uniService = uniService;
        this.skuIdService = skuIdService;
        this.ntuNniSfpOrAaSfpService = ntuNniSfpOrAaSfpService;
        this.ntuTypeService = ntuTypeService;
        this.errorHandlerService = errorHandlerService;
    }

    @JobWorker(type = JOB_TYPE_HARDWARE_TO_BE_SHIPPED)
    public void hardwareToBeShipped(final JobClient client, final ActivatedJob job) {
        Map<String, Object> output = new HashMap<>();

        // Create a safe logging map
        Map<String, Object> safeLogMap = new HashMap<>();
        safeLogMap.put("jobKey", job.getKey());
        safeLogMap.put("jobType", JOB_TYPE_HARDWARE_TO_BE_SHIPPED);

        try {
            String networkElement = job.getVariable(NETWORK_ELEMENT).toString();
            String distance = job.getVariable(DISTANCE).toString();
            String ntuRequired = job.getVariable(NTU_REQUIRED).toString();
            String ntuSize = job.getVariable(NTU_SIZE).toString();
            String uniPortCapacity = job.getVariable(UNI_PORT_CAPACITY).toString();
            String uniInterfaceType = job.getVariable(UNI_INTERFACE_TYPE).toString();

            // Add non-PII data to safe logging map
            safeLogMap.put("ntuRequired", ntuRequired);
            safeLogMap.put("uniPortCapacity", uniPortCapacity);
            safeLogMap.put("distance", distance);

            // Log safely without exposing PII
            LoggingUtil.logSafely(logger, "info", "Processing HardwareToBeShippedWorker job", safeLogMap);

            if ("No".equalsIgnoreCase(ntuRequired)) {
                handleNoNtuCase(output, networkElement, distance, uniPortCapacity, uniInterfaceType, ntuRequired);
            } else if ("Yes".equalsIgnoreCase(ntuRequired)) {
                handleYesNtuCase(output, networkElement, distance, ntuSize, uniPortCapacity, uniInterfaceType, ntuRequired);
            }

            output.put(ERROR_MESSAGE, null);

            // Create a logging map with only non-PII information about the results
            Map<String, Object> resultLogMap = new HashMap<>();
            resultLogMap.put("jobKey", job.getKey());
            resultLogMap.put("ntuRequired", ntuRequired);
            resultLogMap.put("success", true);
            resultLogMap.put("outputFieldsCount", output.size());

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            LoggingUtil.logSafely(logger, "info", "Job completed successfully", resultLogMap);

        } catch (Exception e) {
            // Use sanitized error logging
            String safeErrorMessage = LoggingUtil.sanitizeMessage(e.getMessage());
            logger.error("Error processing {} job: {}", JOB_TYPE_HARDWARE_TO_BE_SHIPPED, safeErrorMessage);

            output = errorHandlerService.handleError(e, JOB_TYPE_HARDWARE_TO_BE_SHIPPED);

            Map<String, Object> errorLogMap = new HashMap<>();
            errorLogMap.put("jobKey", job.getKey());
            errorLogMap.put("errorCode", output.containsKey(ERROR_CODE) ? output.get(ERROR_CODE) : "UNKNOWN");

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            LoggingUtil.logSafely(logger, "error", "Job completed with error", errorLogMap);
        }
    }

    private void handleNoNtuCase(Map<String, Object> output, String networkElement, String distance,
                                 String uniPortCapacity, String uniInterfaceType, String ntuRequired) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("ntuRequired", ntuRequired);

        String distanceRanges = mapDistanceToDatabaseValue(distance);
        String vendorType = Optional.ofNullable(networkElementService.getVendorType(networkElement))
                .orElseThrow(() -> new IllegalArgumentException("No Vendor Type found for networkElement: " + networkElement));

        logMap.put("vendorTypeFound", vendorType != null);
        LoggingUtil.logSafely(logger, "debug", "Processing No NTU case", logMap);

        String aaUniSfp = uniService.getAaUniSfp(distanceRanges, ntuRequired, "0", vendorType, uniPortCapacity, uniInterfaceType);
        if (aaUniSfp == null) {
            throw new IllegalArgumentException("No aaUniSfp found for the given parameters");
        }

        String skuIdValue = Optional.ofNullable(skuIdService.getSkuIdByAaUniSfp(aaUniSfp))
                .map(SkuId::getAaUniSfpSkuId)
                .orElseThrow(() -> new IllegalArgumentException("No skuId found for aaUniSfp: " + aaUniSfp));

        output.put(AA_UNI_SFP, aaUniSfp);
        output.put(SKU_ID, skuIdValue);
        output.put(NTU_REQUIRED, ntuRequired);

        // Log success with minimal information
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("hasAaUniSfp", true);
        resultMap.put("hasSkuId", true);
        LoggingUtil.logSafely(logger, "debug", "No NTU case processed successfully", resultMap);
    }

    private void handleYesNtuCase(Map<String, Object> output, String networkElement, String distance,
                                  String ntuSize, String uniPortCapacity, String uniInterfaceType, String ntuRequired) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("ntuRequired", ntuRequired);
        logMap.put("ntuSize", "*** exists ***");

        String vendorType = Optional.ofNullable(networkElementService.getVendorType(networkElement))
                .orElseThrow(() -> new IllegalArgumentException("No Vendor Type found for networkElement: " + networkElement));

        String distanceRanges = mapDistanceToDatabaseValue(distance);

        logMap.put("vendorTypeFound", vendorType != null);
        LoggingUtil.logSafely(logger, "debug", "Processing Yes NTU case", logMap);

        // Get NTU Type
        NtuType ntuType = Optional.ofNullable(ntuTypeService.getNtuTypeBySize(ntuSize))
                .orElseThrow(() -> new IllegalArgumentException("No NTU Type found for size: " + ntuSize));

        String ntuTypeValue = ntuType.getNtuType();
        String ntuTypeSkuIdValue = Optional.ofNullable(skuIdService.getSkuIdByAaUniSfp(ntuTypeValue))
                .map(SkuId::getAaUniSfpSkuId)
                .orElseThrow(() -> new IllegalArgumentException("No SKU ID found for NTU Type: " + ntuTypeValue));

        // Get NTU NNI SFP
        String ntuNniSfp = Optional.ofNullable(ntuNniSfpOrAaSfpService.getNtuNniSfp(ntuSize, distanceRanges, vendorType))
                .orElseThrow(() -> new IllegalArgumentException("No NTU NNI SFP found for the given parameters"));

        String ntuNniSfpSkuIdValue = Optional.ofNullable(skuIdService.getSkuIdByAaUniSfp(ntuNniSfp))
                .map(SkuId::getAaUniSfpSkuId)
                .orElseThrow(() -> new IllegalArgumentException("No SkuId found for NTU NNI SFP: " + ntuNniSfp));

        // Get AA SFP
        String aaSfp = Optional.ofNullable(ntuNniSfpOrAaSfpService.getAaSfp(ntuSize, distanceRanges, vendorType))
                .orElseThrow(() -> new IllegalArgumentException("No AA SFP found for the given parameters"));

        String aaSfpSkuIdValue = Optional.ofNullable(skuIdService.getSkuIdByAaUniSfp(aaSfp))
                .map(SkuId::getAaUniSfpSkuId)
                .orElseThrow(() -> new IllegalArgumentException("No SkuId found for AA SFP: " + aaSfp));

        // Get AA UNI SFP
        String aaUniSfp = Optional.ofNullable(uniService.getAaUniSfp(distanceRanges, ntuRequired, ntuSize, vendorType, uniPortCapacity, uniInterfaceType))
                .orElseThrow(() -> new IllegalArgumentException("No AA UNI SFP found for given parameters"));

        String skuIdValue = Optional.ofNullable(skuIdService.getSkuIdByAaUniSfp(aaUniSfp))
                .map(SkuId::getAaUniSfpSkuId)
                .orElseThrow(() -> new IllegalArgumentException("No SkuId found for AA UNI SFP: " + aaUniSfp));

        // Set all output values
        output.put(NTU_TYPE, ntuTypeValue);
        output.put(NTU_TYPE_SKU_ID, ntuTypeSkuIdValue);
        output.put(NTU_NNI_SFP, ntuNniSfp);
        output.put(NTU_NNI_SFP_SKU_ID, ntuNniSfpSkuIdValue);
        output.put(AA_SFP, aaSfp);
        output.put(AA_SFP_SKU_ID, aaSfpSkuIdValue);
        output.put(AA_UNI_SFP, aaUniSfp);
        output.put(SKU_ID, skuIdValue);
        output.put(NTU_REQUIRED, ntuRequired);

        // Log success with minimal information
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("allComponentsFound", true);
        resultMap.put("componentsCount", 8);
        LoggingUtil.logSafely(logger, "debug", "Yes NTU case processed successfully", resultMap);
    }

    private String mapDistanceToDatabaseValue(String distance) {
        try {
            int numericDistance = Integer.parseInt(distance.trim());
            String result;

            if (numericDistance < 100) {
                result = "distance_ranges = '<100' OR distance_ranges = '< 300' OR distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance < 300) {
                result = "distance_ranges = '< 300' OR distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance < 500) {
                result = "distance_ranges = '< 500' OR distance_ranges = '<10000'";
            } else if (numericDistance < 10000) {
                result = "distance_ranges = '<10000'";
            } else if (numericDistance < 40000) {
                result = "distance_ranges = '>=10000 and <40000'";
            } else if (numericDistance < 80000) {
                result = "distance_ranges = '>=40000 and <80000'";
            } else {
                result = "distance_ranges = 'incorrect distance value'";
            }

            // Log distance mapping without exposing actual value
            Map<String, Object> distanceLogMap = new HashMap<>();
            distanceLogMap.put("distanceRangeFound", true);
            distanceLogMap.put("distanceCategory", numericDistance < 10000 ? "short" :
                    (numericDistance < 40000 ? "medium" : "long"));
            LoggingUtil.logSafely(logger, "debug", "Mapped distance to database value", distanceLogMap);

            return result;
        } catch (NumberFormatException e) {
            logger.error("Invalid distance value format: {}", LoggingUtil.sanitizeMessage(distance));
            return null;
        }
    }
}