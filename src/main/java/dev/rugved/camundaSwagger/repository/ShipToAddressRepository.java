package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.config.QueryConfig;
import dev.rugved.camundaSwagger.entity.OtherAddress;
import dev.rugved.camundaSwagger.entity.ShipToAddress;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static dev.rugved.camundaSwagger.util.Constants.*;

/**
 * Repository for accessing shipping address data from the database.
 */
@Repository
public class ShipToAddressRepository {

    private static final Logger logger = LoggerFactory.getLogger(ShipToAddressRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public ShipToAddressRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    /**
     * Finds all ship-to addresses for a specific state or province.
     *
     * @param stateOrProvince The state or province to search for
     * @return List of ShipToAddress objects
     */
    public List<ShipToAddress> findAddressesByState(String stateOrProvince) {
        try {
            String queryStr = queryConfig.getShipToAddressQueries().get(FIND_ADDRESSES_BY_STATE);
            if (queryStr == null) {
                logger.error("Query for FIND_ADDRESSES_BY_STATE not found in configuration");
                return new ArrayList<>();
            }

            Query query = entityManager.createNativeQuery(queryStr);
            query.setParameter(STATE_OR_PROVINCE, stateOrProvince);

            logger.debug("Executing query to find addresses by state: {}", stateOrProvince);

            List<Object[]> rawResults = query.getResultList();
            List<ShipToAddress> addresses = new ArrayList<>();

            for (Object[] result : rawResults) {
                ShipToAddress address = new ShipToAddress();
                address.setShipToAddressId(result[0].toString());
                address.setShipToAddressRole(result[1] != null ? result[1].toString() : null);
                addresses.add(address);
            }

            return addresses;
        } catch (Exception e) {
            logger.error("Error finding addresses for state: {}", stateOrProvince, e);
            return new ArrayList<>();
        }
    }

    /**
     * Finds all other address types (sold-to, network site, additional partner).
     *
     * @return List of OtherAddress objects
     */
    public List<OtherAddress> findOtherAddresses() {
        try {
            String queryStr = queryConfig.getShipToAddressQueries().get(FIND_OTHER_ADDRESSES);
            if (queryStr == null) {
                logger.error("Query for FIND_OTHER_ADDRESSES not found in configuration");
                return new ArrayList<>();
            }

            Query query = entityManager.createNativeQuery(queryStr);

            logger.debug("Executing query to find other addresses");

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
        } catch (Exception e) {
            logger.error("Error finding other addresses", e);
            return new ArrayList<>();
        }
    }
}