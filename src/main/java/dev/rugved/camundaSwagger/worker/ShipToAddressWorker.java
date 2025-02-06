package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.entity.OtherAddress;
import dev.rugved.camundaSwagger.entity.ShipToAddress;
import dev.rugved.camundaSwagger.service.ShipToAddressService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            String stateOrProvince = job.getVariable("stateOrProvince").toString();
            logger.info("State or Province: {}", stateOrProvince);

            // Fetch details from database
            List<ShipToAddress> addresses = shipToAddressService.getShipToAddressesByState(stateOrProvince);

            List<OtherAddress> otherAddresses = shipToAddressService.getOtherAddresses();

            // Prepare the output map
            Map<String, Object> output = new HashMap<>();
            if (!addresses.isEmpty()) {
                output.put("shipToAddresses", addresses.stream()
                        .map(address -> Map.of(
                                "shipToAddressId", address.getShipToAddressId(),
                                "shipToAddressRole", address.getShipToAddressRole()))
                        .toList());
            } else {
                throw new IllegalArgumentException("No shipToAddresses details found for state: " + stateOrProvince);
            }

            output.put("stateOrProvince", stateOrProvince);

            if (!otherAddresses.isEmpty()) {
                output.put("otherAddresses", otherAddresses.stream()
                        .map(address -> Map.of(
                                "soldToAddressRole", address.getSoldToAddressRole(),
                                "soldToAddressId", address.getSoldToAddressId(),
                                "networkSiteAddressRole", address.getNetworkSiteAddressRole(),
                                "networkSiteAddressId", address.getNetworkSiteAddressId(),
                                "additionalPartnerAddressRole", address.getAdditionalPartnerAddressRole(),
                                "additionalPartnerAddressId", address.getAdditionalPartnerAddressId()))
                        .toList());
            } else {
                throw new IllegalArgumentException("No otherAddresses details found");
            }

            client.newCompleteCommand(job.getKey()).variables(output).send().join();

            logger.info("Job completed with variables: {}", output);

        } catch (Exception e) {
            logger.error("Error processing fetchWBSDetails job", e);
            Map<String, Object> output = new HashMap<>();
            output.put("errorMessage", " " + e.getMessage());

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        }
    }
}
