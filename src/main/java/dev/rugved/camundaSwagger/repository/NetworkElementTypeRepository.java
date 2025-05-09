package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.NetworkElementType;
import dev.rugved.camundaSwagger.config.QueryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Repository
public class NetworkElementTypeRepository {

    private static final Logger logger = LoggerFactory.getLogger(NetworkElementTypeRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public NetworkElementTypeRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public Optional<NetworkElementType> findByNetworkElement(String networkElement) {
        if (networkElement == null) {
            logger.warn("Null network element provided to repository");
            return Optional.empty();
        }

        try {
            String queryStr = queryConfig.getNetworkElementType().get(FIND_BY_NETWORK_ELEMENT);
            if (queryStr == null) {
                logger.error("Query for FIND_BY_NETWORK_ELEMENT not found in configuration");
                return Optional.empty();
            }

            TypedQuery<NetworkElementType> query = entityManager.createQuery(queryStr, NetworkElementType.class);
            query.setParameter(NETWORK_ELEMENT, networkElement);

            return query.getResultList().stream().findFirst();
        } catch (NoResultException e) {
            logger.debug("No network element found for: {}", networkElement);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding network element: {}", networkElement, e);
            return Optional.empty();
        }
    }
}