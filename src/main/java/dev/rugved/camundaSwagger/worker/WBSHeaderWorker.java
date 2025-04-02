package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.service.WBSHeaderService;
import dev.rugved.camundaSwagger.service.ErrorHandlerService;
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

        try {
            String stateOrProvince = job.getVariable(STATE_OR_PROVINCE).toString();

            // Log job processing without exposing the actual state/province
            logger.info("Processing fetchWBSDetails job - jobKey: {}, jobType: {}",
                    job.getKey(), JOB_TYPE_FETCH_WBS_DETAILS);

            var wbsHeaderDetails = service.getWBSHeaderDetailsByState(stateOrProvince);
            if (wbsHeaderDetails == null) {
                throw new IllegalArgumentException("No WBS Header details found for state: " + stateOrProvince);
            }

            // Log successful retrieval without exposing actual data
            logger.debug("Retrieved WBS header details - jobKey: {}, wbsHeaderFound: true, " +
                            "customerTypeAvailable: {}, customerSubTypeAvailable: {}",
                    job.getKey(),
                    wbsHeaderDetails.getCustomerType() != null,
                    wbsHeaderDetails.getCustomerSubType() != null);

            output.put(WBS_HEADER, wbsHeaderDetails.getWbsHeader());
            output.put(CUSTOMER_TYPE, wbsHeaderDetails.getCustomerType());
            output.put(CUSTOMER_SUBTYPE, wbsHeaderDetails.getCustomerSubType());
            output.put(ERROR_MESSAGE, null);

            // Log completion
            logger.info("fetchWBSDetails job completed successfully - jobKey: {}, status: completed, outputSize: {}",
                    job.getKey(), output.size());

            client.newCompleteCommand(job.getKey()).variables(output).send().join();

        } catch (Exception e) {
            // Log error
            logger.error("Error processing fetchWBSDetails job: {}", e.getMessage());

            output = errorHandlerService.handleError(e, JOB_TYPE_FETCH_WBS_DETAILS);

            logger.error("fetchWBSDetails job completed with error - jobKey: {}, errorCode: {}",
                    job.getKey(), output.containsKey(ERROR_CODE) ? output.get(ERROR_CODE) : "UNKNOWN");

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        }
    }
}