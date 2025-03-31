package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.SkuId;
import dev.rugved.camundaSwagger.config.QueryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static dev.rugved.camundaSwagger.util.Constants.*;

/**
 * Repository for accessing SKU ID data from the database.
 */
@Repository
public class SkuIdRepository {

    private static final Logger logger = LoggerFactory.getLogger(SkuIdRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public SkuIdRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    /**
     * Finds a SKU ID entity by the AA UNI SFP identifier.
     *
     * @param aaUniSfp The AA UNI SFP identifier
     * @return Optional containing the SkuId if found, empty Optional otherwise
     */
    public Optional<SkuId> findByAaUniSfp(String aaUniSfp) {
        if (aaUniSfp == null) {
            logger.warn("Null AA UNI SFP provided to repository");
            return Optional.empty();
        }

        try {
            String queryStr = queryConfig.getSkuId().get(FIND_BY_AA_UNI_SFP).toString();
            if (queryStr == null) {
                logger.error("Query for FIND_BY_AA_UNI_SFP not found in configuration");
                return Optional.empty();
            }

            TypedQuery<SkuId> query = entityManager.createQuery(queryStr, SkuId.class);
            query.setParameter(AA_UNI_SFP, aaUniSfp);

            return query.getResultList().stream().findFirst();
        } catch (NoResultException e) {
            logger.debug("No SKU ID found for AA UNI SFP: {}", aaUniSfp);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding SKU ID for AA UNI SFP: {}", aaUniSfp, e);
            return Optional.empty();
        }
    }
}