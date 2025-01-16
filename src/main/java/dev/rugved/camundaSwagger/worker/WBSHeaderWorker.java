package dev.rugved.camundaSwagger.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rugved.camundaSwagger.service.WBSHeaderService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component
public class WBSHeaderWorker {

    private static final Logger logger = LoggerFactory.getLogger(WBSHeaderWorker.class);

    @Autowired
    private WBSHeaderService service;

    @JobWorker(type = "fetchWBSDetails")
    public void fetchWBSDetails(final JobClient client, final ActivatedJob job) {
        try {
            // Fetch and parse variables
            String var = job.getVariables();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(var);

            // Extract stateOrProvince
            JsonNode stateNode = rootNode.path("relatedParty").get(1)
                    .path("contactMedium").get(0)
                    .path("characteristic")
                    .path("stateOrProvince");

            if (stateNode.isMissingNode() || stateNode.isNull()) {
                throw new IllegalArgumentException("State or Province not found in input JSON");
            }

            String stateOrProvince = stateNode.asText();
            logger.info("State or Province: {}", stateOrProvince);

            // Fetch WBSHeader details from the service
            var wbsHeaderDetails = service.getWBSHeaderDetailsByState(stateOrProvince);
            if (wbsHeaderDetails == null) {
                throw new IllegalArgumentException("No WBS Header details found for state: " + stateOrProvince);
            }

            logger.info("WBS Header: {}, Customer Type: {}, Customer Sub-Type: {}",
                    wbsHeaderDetails.getWbsHeader(),
                    wbsHeaderDetails.getCustomerType(),
                    wbsHeaderDetails.getCustomerSubType());

            // Prepare the output map
            Map<String, Object> output = new HashMap<>();
            output.put("wbsHeader", wbsHeaderDetails.getWbsHeader());
            output.put("customerType", wbsHeaderDetails.getCustomerType());
            output.put("customerSubType", wbsHeaderDetails.getCustomerSubType());

            // Complete the job and send variables back to Zeebe
            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            logger.info("Job completed with variables: {}", output);

        } catch (Exception e) {
            logger.error("Error processing fetchWBSDetails job", e);
        }
    }
}
