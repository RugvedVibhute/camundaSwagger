package dev.rugved.camundaSwagger.worker;

import dev.rugved.camundaSwagger.entity.OtherAddress;
import dev.rugved.camundaSwagger.entity.ShipToAddress;
import dev.rugved.camundaSwagger.service.ShipToAddressService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Component
public class ShipToAddressWorker {

    private static final Logger logger = LoggerFactory.getLogger(ShipToAddressWorker.class);

    private final ShipToAddressService shipToAddressService;

    public ShipToAddressWorker(ShipToAddressService shipToAddressService) {
        this.shipToAddressService = shipToAddressService;
    }

    @JobWorker(type = JOB_TYPE_SHIP_TO_ADDRESS)
    public void shipToAddress(final JobClient client, final ActivatedJob job) {
        try {
            String stateOrProvince = job.getVariable(STATE_OR_PROVINCE).toString();
            logger.info("Processing shipToAddress job | StateOrProvince: {}", stateOrProvince);

            List<ShipToAddress> addresses = shipToAddressService.getShipToAddressesByState(stateOrProvince);
            List<OtherAddress> otherAddresses = shipToAddressService.getOtherAddresses();

            if (addresses.isEmpty()) {
                throw new IllegalArgumentException("No shipToAddresses details found for state: " + stateOrProvince);
            }

            if (otherAddresses.isEmpty()) {
                throw new IllegalArgumentException("No otherAddresses details found");
            }

            Map<String, Object> output = Map.of(
                    SHIP_TO_ADDRESSES, addresses.stream()
                            .map(address -> Map.of(
                                    SHIP_TO_ADDRESS_ID, address.getShipToAddressId(),
                                    SHIP_TO_ADDRESS_ROLE, address.getShipToAddressRole()))
                            .toList(),
                    STATE_OR_PROVINCE, stateOrProvince,
                    OTHER_ADDRESSES, otherAddresses.stream()
                            .map(address -> Map.of(
                                    SOLD_TO_ADDRESS_ROLE, address.getSoldToAddressRole(),
                                    SOLD_TO_ADDRESS_ID, address.getSoldToAddressId(),
                                    NETWORK_SITE_ADDRESS_ROLE, address.getNetworkSiteAddressRole(),
                                    NETWORK_SITE_ADDRESS_ID, address.getNetworkSiteAddressId(),
                                    ADDITIONAL_PARTNER_ADDRESS_ROLE, address.getAdditionalPartnerAddressRole(),
                                    ADDITIONAL_PARTNER_ADDRESS_ID, address.getAdditionalPartnerAddressId()))
                            .toList()
            );

            client.newCompleteCommand(job.getKey()).variables(output).send().join();
            logger.info("shipToAddress job completed successfully | Variables: {}", output);

        } catch (Exception e) {
            logger.error("Error processing shipToAddress job: {}", e.getMessage(), e);

            Map<String, Object> errorOutput = Map.of(ERROR_MESSAGE, e.getMessage());
            client.newCompleteCommand(job.getKey()).variables(errorOutput).send().join();
        }
    }
}
