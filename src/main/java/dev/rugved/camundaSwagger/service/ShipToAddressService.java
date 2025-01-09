package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.ShipToAddress;

import java.util.List;

public interface ShipToAddressService {
    List<ShipToAddress> getShipToAddressesByState(String stateOrProvince);
}
