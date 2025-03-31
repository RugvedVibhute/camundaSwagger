package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.OtherAddress;
import dev.rugved.camundaSwagger.entity.ShipToAddress;

import java.util.List;

/**
 * Service interface for retrieving shipping address information.
 */
public interface ShipToAddressService {

    /**
     * Retrieves a list of ship-to addresses for the given state or province.
     *
     * @param stateOrProvince The state or province to search for
     * @return List of ShipToAddress objects for the given state
     */
    List<ShipToAddress> getShipToAddressesByState(String stateOrProvince);

    /**
     * Retrieves a list of other addresses such as sold-to, network site, and additional partner addresses.
     *
     * @return List of OtherAddress objects
     */
    List<OtherAddress> getOtherAddresses();
}