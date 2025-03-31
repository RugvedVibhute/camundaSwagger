package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.config.QueryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static dev.rugved.camundaSwagger.util.Constants.*;

/**
 * Repository for accessing NTU NNI SFP and AA SFP data from the database.
 */
@Repository
public class NtuNniSfpOrAaSfpRepository {

    private static final Logger logger = LoggerFactory.getLogger(NtuNniSfpOrAaSfpRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public NtuNniSfpOrAaSfpRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    /**
     * Finds the NTU NNI SFP value based on the provided parameters.
     *
     * @param ntuSize The size of the NTU
     * @param distanceRanges The distance range constraints
     * @param vendorType The vendor type
     * @return The NTU NNI SFP value or null if not found
     */
    public String findNtuNniSfp(String ntuSize, String distanceRanges, String vendorType) {
        try {
            String queryStr = queryConfig.getNtuNniSfpOrAaSfp().get(FIND_NTU_NNI_SFP);
            if (queryStr == null) {
                logger.error("Query for FIND_NTU_NNI_SFP not found in configuration");
                return null;
            }

            TypedQuery<String> query = entityManager.createQuery(queryStr, String.class);
            query.setParameter(NTU_SIZE, ntuSize);
            query.setParameter(DISTANCE_RANGES, "%" + distanceRanges + "%");
            query.setParameter(VENDOR_TYPE, "%" + vendorType + "%");

            logger.debug("Executing NTU NNI SFP query with parameters: ntuSize={}, distanceRanges={}, vendorType={}",
                    ntuSize, distanceRanges, vendorType);

            return query.getResultList().stream().findFirst().orElse(null);
        } catch (NoResultException e) {
            logger.debug("No NTU NNI SFP found for the given parameters: ntuSize={}, distanceRanges={}, vendorType={}",
                    ntuSize, distanceRanges, vendorType);
            return null;
        } catch (Exception e) {
            logger.error("Error finding NTU NNI SFP: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Finds the AA SFP value based on the provided parameters.
     *
     * @param ntuSize The size of the NTU
     * @param distanceRanges The distance range constraints
     * @param vendorType The vendor type
     * @return The AA SFP value or null if not found
     */
    public String findAaSfp(String ntuSize, String distanceRanges, String vendorType) {
        try {
            String queryStr = queryConfig.getNtuNniSfpOrAaSfp().get(FIND_AA_SFP);
            if (queryStr == null) {
                logger.error("Query for FIND_AA_SFP not found in configuration");
                return null;
            }

            TypedQuery<String> query = entityManager.createQuery(queryStr, String.class);
            query.setParameter(NTU_SIZE, ntuSize);
            query.setParameter(DISTANCE_RANGES, "%" + distanceRanges + "%");
            query.setParameter(VENDOR_TYPE, "%" + vendorType + "%");

            logger.debug("Executing AA SFP query with parameters: ntuSize={}, distanceRanges={}, vendorType={}",
                    ntuSize, distanceRanges, vendorType);

            return query.getResultList().stream().findFirst().orElse(null);
        } catch (NoResultException e) {
            logger.debug("No AA SFP found for the given parameters: ntuSize={}, distanceRanges={}, vendorType={}",
                    ntuSize, distanceRanges, vendorType);
            return null;
        } catch (Exception e) {
            logger.error("Error finding AA SFP: {}", e.getMessage(), e);
            return null;
        }
    }
}