package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.config.QueryConfig;
import dev.rugved.camundaSwagger.entity.OtherAddress;
import dev.rugved.camundaSwagger.entity.ShipToAddress;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Repository
public class ShipToAddressRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public ShipToAddressRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public List<ShipToAddress> findAddressesByState(String stateOrProvince) {
        String queryStr = queryConfig.getShipToAddressQueries().get(FIND_ADDRESSES_BY_STATE);

        Query query = entityManager.createNativeQuery(queryStr);
        query.setParameter(STATE_OR_PROVINCE, stateOrProvince);

        List<Object[]> rawResults = query.getResultList();
        List<ShipToAddress> addresses = new ArrayList<>();

        for (Object[] result : rawResults) {
            ShipToAddress address = new ShipToAddress();
            address.setShipToAddressId(result[0].toString());
            address.setShipToAddressRole(result[1] != null ? result[1].toString() : null);
            addresses.add(address);
        }

        return addresses;
    }

    public List<OtherAddress> findOtherAddresses() {
        String queryStr = queryConfig.getShipToAddressQueries().get(FIND_OTHER_ADDRESSES);

        Query query = entityManager.createNativeQuery(queryStr);
        List<Object[]> rawResults = query.getResultList();
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
