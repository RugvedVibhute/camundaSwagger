package dev.rugved.camundaSwagger.service;

import dev.rugved.camundaSwagger.entity.OtherAddress;
import dev.rugved.camundaSwagger.entity.ShipToAddress;
import dev.rugved.camundaSwagger.repository.ShipToAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        List<Object[]> rawResults = shipToAddressRepository.findAddressesByStateNative(stateOrProvince);
        List<ShipToAddress> addresses = new ArrayList<>();

        for (Object[] result : rawResults) {
            ShipToAddress address = new ShipToAddress();

            // Safely parse and assign fields
            address.setShipToAddressId(result[0].toString()); // Convert to Long
            address.setShipToAddressRole(result[1] != null ? result[1].toString() : null);

            addresses.add(address);
        }

        return addresses;
    }

    @Override
    public List<OtherAddress> getOtherAddresses() {
        List<Object[]> rawResults = shipToAddressRepository.findOtherAddresses();
        List<OtherAddress> addresses = new ArrayList<>();

        for (Object[] result : rawResults) {
            OtherAddress address = new OtherAddress();

            address.setSoldToAddressRole(result[0] != null ? result[0].toString() : null);
            address.setSoldToAddressId(result[1] != null ? result[1].toString() : null);
            address.setNetworkSiteAddressRole(result[2] != null ? result[2].toString() : null);
            address.setNetworkSiteAddressId(result[3] != null ? result[3].toString() : null);
            address.setAdditionalPartnerAddressRole(result[4] != null ? result[4].toString() : null);
            address.setAdditionalPartnerAddressId(result[5] != null ? result[5].toString() : null);

            addresses.add(address);
        }

        return addresses;
    }

}