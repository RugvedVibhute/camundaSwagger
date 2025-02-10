package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.service.WBSHeaderService;
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

    public WBSHeaderWorker(WBSHeaderService service) {
        this.service = service;
    }

    @JobWorker(type = JOB_TYPE_FETCH_WBS_DETAILS)
    public void fetchWBSDetails(final JobClient client, final ActivatedJob job) {
        try {
            String stateOrProvince = job.getVariable(STATE_OR_PROVINCE).toString();
            logger.info("Processing fetchWBSDetails job | StateOrProvince: {}", stateOrProvince);

            var wbsHeaderDetails = service.getWBSHeaderDetailsByState(stateOrProvince);
            if (wbsHeaderDetails == null) {
                throw new IllegalArgumentException("No WBS Header details found for state: " + stateOrProvince);
            }

            Map<String, Object> output = Map.of(
                    WBS_HEADER, wbsHeaderDetails.getWbsHeader(),
                    CUSTOMER_TYPE, wbsHeaderDetails.getCustomerType(),
                    CUSTOMER_SUBTYPE, wbsHeaderDetails.getCustomerSubType()
            );

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            logger.info("fetchWBSDetails job completed successfully | Variables: {}", output);

        } catch (Exception e) {
            logger.error("Error processing fetchWBSDetails job: {}", e.getMessage(), e);

            Map<String, Object> errorOutput = Map.of(ERROR_MESSAGE, e.getMessage());
            client.newCompleteCommand(job.getKey()).variables(errorOutput).send().join();
        }
    }
}
