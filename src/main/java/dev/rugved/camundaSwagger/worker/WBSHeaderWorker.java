package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.service.WBSHeaderService;
import dev.rugved.camundaSwagger.service.ErrorHandlerService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.rugved.camundaSwagger.util.Constants.*;

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
        Map<String, Object> output;
        try {
            String stateOrProvince = job.getVariable(STATE_OR_PROVINCE).toString();
            logger.info("Processing fetchWBSDetails job | StateOrProvince: {}", stateOrProvince);

            var wbsHeaderDetails = service.getWBSHeaderDetailsByState(stateOrProvince);
            if (wbsHeaderDetails == null) {
                throw new IllegalArgumentException("No WBS Header details found for state: " + stateOrProvince);
            }

            output = Map.of(
                    WBS_HEADER, wbsHeaderDetails.getWbsHeader(),
                    CUSTOMER_TYPE, wbsHeaderDetails.getCustomerType(),
                    CUSTOMER_SUBTYPE, wbsHeaderDetails.getCustomerSubType()
            );

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            logger.info("fetchWBSDetails job completed successfully | Variables: {}", output);

        } catch (Exception e) {
            output = errorHandlerService.handleError(e, JOB_TYPE_FETCH_WBS_DETAILS);
            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        }
    }
}
