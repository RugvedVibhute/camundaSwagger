package dev.rugved.camundaSwagger.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rugved.camundaSwagger.entity.ShipToAddress;
import dev.rugved.camundaSwagger.service.ShipToAddressService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShipToAddressWorker {

    private static final Logger logger = LoggerFactory.getLogger(ShipToAddressWorker.class);

    private final ShipToAddressService shipToAddressService;

    @Autowired
    public ShipToAddressWorker(ShipToAddressService shipToAddressService) {
        this.shipToAddressService = shipToAddressService;
    }

    @JobWorker(type = "shipToAddress")
    public void shipToAddress(final JobClient client, final ActivatedJob job) {
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

            // Fetch details from database
            List<ShipToAddress> addresses = shipToAddressService.getShipToAddressesByState(stateOrProvince);
            if (addresses.isEmpty()) {
                logger.warn("No Ship To Address found for state: {}", stateOrProvince);
            } else {
                addresses.forEach(address -> logger.info("Ship To Address ID: {}, Role: {}",
                        address.getShipToAddressId(), address.getShipToAddressRole()));
            }

        } catch (Exception e) {
            logger.error("Error processing job", e);
        }
    }
}

