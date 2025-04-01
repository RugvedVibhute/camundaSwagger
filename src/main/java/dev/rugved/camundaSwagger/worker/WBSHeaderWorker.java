package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.service.WBSHeaderService;
import dev.rugved.camundaSwagger.service.ErrorHandlerService;
import dev.rugved.camundaSwagger.util.LoggingUtil;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static dev.rugved.camundaSwagger.util.Constants.*;

/**
 * Worker for handling WBS Header related job tasks.
 */
@Component
public class WBSHeaderWorker {

    private static final Logger logger = LoggerFactory.getLogger(WBSHeaderWorker.class);
    private final WBSHeaderService service;
    private final ErrorHandlerService errorHandlerService;

    public WBSHeaderWorker(WBSHeaderService service, ErrorHandlerService errorHandlerService) {
        this.service = service;
        this.errorHandlerService = errorHandlerService;
    }

    @JobWorker(type = JOB_TYPE_FETCH_WBS_DETAILS)
    public void fetchWBSDetails(final JobClient client, final ActivatedJob job) {
        Map<String, Object> output = new HashMap<>();

        // Create safe logging map
        Map<String, Object> safeLogMap = new HashMap<>();
        safeLogMap.put("jobKey", job.getKey());
        safeLogMap.put("jobType", JOB_TYPE_FETCH_WBS_DETAILS);

        try {
            String stateOrProvince = job.getVariable(STATE_OR_PROVINCE).toString();

            // Log without exposing PII
            LoggingUtil.logSafely(logger, "info", "Processing fetchWBSDetails job", safeLogMap);

            var wbsHeaderDetails = service.getWBSHeaderDetailsByState(stateOrProvince);
            if (wbsHeaderDetails == null) {
                throw new IllegalArgumentException("No WBS Header details found for state: " + stateOrProvince);
            }

            // Log successful retrieval without exposing actual data
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("jobKey", job.getKey());
            resultMap.put("wbsHeaderFound", true);
            resultMap.put("customerTypeAvailable", wbsHeaderDetails.getCustomerType() != null);
            resultMap.put("customerSubTypeAvailable", wbsHeaderDetails.getCustomerSubType() != null);
            LoggingUtil.logSafely(logger, "debug", "Retrieved WBS header details", resultMap);

            output.put(WBS_HEADER, wbsHeaderDetails.getWbsHeader());
            output.put(CUSTOMER_TYPE, wbsHeaderDetails.getCustomerType());
            output.put(CUSTOMER_SUBTYPE, wbsHeaderDetails.getCustomerSubType());
            output.put(ERROR_MESSAGE, null);

            // Log completion without exposing data
            Map<String, Object> completionMap = new HashMap<>();
            completionMap.put("jobKey", job.getKey());
            completionMap.put("status", "completed");
            completionMap.put("outputSize", output.size());

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            LoggingUtil.logSafely(logger, "info", "fetchWBSDetails job completed successfully", completionMap);

        } catch (Exception e) {
            // Use sanitized error logging
            String safeErrorMessage = LoggingUtil.sanitizeMessage(e.getMessage());
            logger.error("Error processing fetchWBSDetails job: {}", safeErrorMessage);

            output = errorHandlerService.handleError(e, JOB_TYPE_FETCH_WBS_DETAILS);

            Map<String, Object> errorLogMap = new HashMap<>();
            errorLogMap.put("jobKey", job.getKey());
            errorLogMap.put("errorCode", output.containsKey(ERROR_CODE) ? output.get(ERROR_CODE) : "UNKNOWN");

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            LoggingUtil.logSafely(logger, "error", "fetchWBSDetails job completed with error", errorLogMap);
        }
    }
}
