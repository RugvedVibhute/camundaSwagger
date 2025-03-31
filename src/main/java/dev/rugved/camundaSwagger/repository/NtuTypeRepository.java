package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.NtuType;
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
 * Repository for accessing NTU Type data from the database.
 */
@Repository
public class NtuTypeRepository {

    private static final Logger logger = LoggerFactory.getLogger(NtuTypeRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public NtuTypeRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    /**
     * Finds an NTU Type by its size.
     *
     * @param ntuSize The NTU size to search for
     * @return Optional containing the NtuType if found, empty Optional otherwise
     */
    public Optional<NtuType> findByNtuSize(String ntuSize) {
        if (ntuSize == null) {
            logger.warn("Null NTU size provided to repository");
            return Optional.empty();
        }

        try {
            String queryStr = queryConfig.getNtuType().get(FIND_BY_NTU_SIZE);
            if (queryStr == null) {
                logger.error("Query for FIND_BY_NTU_SIZE not found in configuration");
                return Optional.empty();
            }

            TypedQuery<NtuType> query = entityManager.createQuery(queryStr, NtuType.class);
            query.setParameter(NTU_SIZE, ntuSize);

            logger.debug("Executing query to find NTU Type by size: {}", ntuSize);

            return query.getResultList().stream().findFirst();
        } catch (NoResultException e) {
            logger.debug("No NTU Type found for size: {}", ntuSize);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding NTU Type for size: {}", ntuSize, e);
            return Optional.empty();
        }
    }
}