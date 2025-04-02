package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.entity.OtherAddress;
import dev.rugved.camundaSwagger.entity.ShipToAddress;
import dev.rugved.camundaSwagger.service.ShipToAddressService;
import dev.rugved.camundaSwagger.service.ErrorHandlerService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Component
public class ShipToAddressWorker {

    private static final Logger logger = LoggerFactory.getLogger(ShipToAddressWorker.class);

    private final ShipToAddressService shipToAddressService;
    private final ErrorHandlerService errorHandlerService;

    public ShipToAddressWorker(ShipToAddressService shipToAddressService, ErrorHandlerService errorHandlerService) {
        this.shipToAddressService = shipToAddressService;
        this.errorHandlerService = errorHandlerService;
    }

    @JobWorker(type = JOB_TYPE_SHIP_TO_ADDRESS)
    public void shipToAddress(final JobClient client, final ActivatedJob job) {
        Map<String, Object> output = new HashMap<>();

        try {
            String stateOrProvince = job.getVariable(STATE_OR_PROVINCE).toString();

            // Log job processing without exposing the actual state/province
            logger.info("Processing shipToAddress job - jobKey: {}, jobType: {}",
                    job.getKey(), JOB_TYPE_SHIP_TO_ADDRESS);

            List<ShipToAddress> addresses = shipToAddressService.getShipToAddressesByState(stateOrProvince);
            List<OtherAddress> otherAddresses = shipToAddressService.getOtherAddresses();

            // Log address counts
            logger.debug("Retrieved addresses - jobKey: {}, addressesFound: {}, otherAddressesFound: {}",
                    job.getKey(), addresses.size(), otherAddresses.size());

            if (addresses.isEmpty()) {
                throw new IllegalArgumentException("No shipToAddresses details found for state: " + stateOrProvince);
            }

            if (otherAddresses.isEmpty()) {
                throw new IllegalArgumentException("No otherAddresses details found");
            }

            // Create output map with address information
            output.put(SHIP_TO_ADDRESSES, addresses.stream()
                    .map(address -> Map.of(
                            SHIP_TO_ADDRESS_ID, address.getShipToAddressId(),
                            SHIP_TO_ADDRESS_ROLE, address.getShipToAddressRole()))
                    .toList());

            output.put(STATE_OR_PROVINCE, stateOrProvince);

            output.put(OTHER_ADDRESSES, otherAddresses.stream()
                    .map(address -> Map.of(
                            SOLD_TO_ADDRESS_ROLE, address.getSoldToAddressRole(),
                            SOLD_TO_ADDRESS_ID, address.getSoldToAddressId(),
                            NETWORK_SITE_ADDRESS_ROLE, address.getNetworkSiteAddressRole(),
                            NETWORK_SITE_ADDRESS_ID, address.getNetworkSiteAddressId(),
                            ADDITIONAL_PARTNER_ADDRESS_ROLE, address.getAdditionalPartnerAddressRole(),
                            ADDITIONAL_PARTNER_ADDRESS_ID, address.getAdditionalPartnerAddressId()))
                    .toList());

            output.put(ERROR_MESSAGE, null);

            // Log successful completion
            logger.info("shipToAddress job completed successfully - jobKey: {}, status: completed, outputSize: {}",
                    job.getKey(), output.size());

            client.newCompleteCommand(job.getKey()).variables(output).send().join();

        } catch (Exception e) {
            // Log error with sanitized message
            logger.error("Error processing shipToAddress job: {}", e.getMessage());

            output = errorHandlerService.handleError(e, JOB_TYPE_SHIP_TO_ADDRESS);

            logger.error("shipToAddress job completed with error - jobKey: {}, errorCode: {}",
                    job.getKey(), output.containsKey(ERROR_CODE) ? output.get(ERROR_CODE) : "UNKNOWN");

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
        }
    }
}