package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.OtherAddress;
import dev.rugved.camundaSwagger.entity.ShipToAddress;
import dev.rugved.camundaSwagger.repository.ShipToAddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ShipToAddressService interface.
 */
@Service
public class ShipToAddressServiceImpl implements ShipToAddressService {

    private static final Logger logger = LoggerFactory.getLogger(ShipToAddressServiceImpl.class);

    private final ShipToAddressRepository repository;

    @Autowired
    public ShipToAddressServiceImpl(ShipToAddressRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ShipToAddress> getShipToAddressesByState(String stateOrProvince) {
        if (stateOrProvince == null || stateOrProvince.trim().isEmpty()) {
            logger.warn("Empty or null stateOrProvince provided");
            return new ArrayList<>();
        }

        logger.info("Fetching ShipToAddresses for state: {}", stateOrProvince);
        return repository.findAddressesByState(stateOrProvince);
    }

    @Override
    public List<OtherAddress> getOtherAddresses() {
        logger.info("Fetching Other Addresses");
        return repository.findOtherAddresses();
    }
}