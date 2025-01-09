package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.ShipToAddress;
import dev.rugved.camundaSwagger.repository.ShipToAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipToAddressServiceImpl implements ShipToAddressService {

    private final ShipToAddressRepository shipToAddressRepository;

    @Autowired
    public ShipToAddressServiceImpl(ShipToAddressRepository shipToAddressRepository) {
        this.shipToAddressRepository = shipToAddressRepository;
    }

    @Override
    public List<ShipToAddress> getShipToAddressesByState(String stateOrProvince) {
        return shipToAddressRepository.findByStateOrProvince(stateOrProvince);
    }
}

