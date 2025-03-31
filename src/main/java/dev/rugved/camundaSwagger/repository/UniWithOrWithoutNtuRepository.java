package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.config.QueryConfig;
import dev.rugved.camundaSwagger.entity.UniWithOrWithoutNtu;
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
 * Repository for accessing UNI with or without NTU data.
 */
@Repository
public class UniWithOrWithoutNtuRepository {

    private static final Logger logger = LoggerFactory.getLogger(UniWithOrWithoutNtuRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public UniWithOrWithoutNtuRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    /**
     * Finds the AA UNI SFP based on the provided parameters.
     *
     * @param distanceRanges    The distance range constraint
     * @param ntuRequired       Whether NTU is required ("Yes" or "No")
     * @param ntuSize           The size of the NTU (if required)
     * @param vendorType        The vendor type
     * @param uniPortCapacity   The UNI port capacity
     * @param uniInterfaceType  The UNI interface type
     * @return The AA UNI SFP identifier or null if not found
     */
    public String findAaUniSfp(String distanceRanges, String ntuRequired, String ntuSize,
                               String vendorType, String uniPortCapacity, String uniInterfaceType) {
        try {
            String queryStr = queryConfig.getUniWithOrWithoutNtu().get(FIND_AA_UNI_SFP);
            if (queryStr == null) {
                logger.error("Query for FIND_AA_UNI_SFP not found in configuration");
                return null;
            }

            TypedQuery<String> query = entityManager.createQuery(queryStr, String.class);
            query.setParameter(DISTANCE_RANGES, distanceRanges);
            query.setParameter(NTU_REQUIRED, ntuRequired);
            query.setParameter(NTU_SIZE, ntuSize);
            query.setParameter(VENDOR_TYPE, "%" + vendorType + "%");
            query.setParameter(UNI_PORT_CAPACITY, uniPortCapacity);
            query.setParameter(UNI_INTERFACE_TYPE, uniInterfaceType);

            logger.debug("Executing query with parameters: distanceRanges={}, ntuRequired={}, " +
                            "ntuSize={}, vendorType={}, uniPortCapacity={}, uniInterfaceType={}",
                    distanceRanges, ntuRequired, ntuSize, vendorType, uniPortCapacity, uniInterfaceType);

            return query.getResultList().stream().findFirst().orElse(null);
        } catch (NoResultException e) {
            logger.debug("No AA UNI SFP found for the given parameters");
            return null;
        } catch (Exception e) {
            logger.error("Error finding AA UNI SFP: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Finds a UniWithOrWithoutNtu entity by its AA UNI SFP identifier.
     *
     * @param aaUniSfp The AA UNI SFP identifier
     * @return An Optional containing the UniWithOrWithoutNtu if found, or empty if not found
     */
    public Optional<UniWithOrWithoutNtu> findByAaUniSfp(String aaUniSfp) {
        try {
            String queryStr = queryConfig.getUniWithOrWithoutNtu().get(FIND_BY_AA_UNI_SFP);
            if (queryStr == null) {
                logger.error("Query for FIND_BY_AA_UNI_SFP not found in configuration");
                return Optional.empty();
            }

            TypedQuery<UniWithOrWithoutNtu> query = entityManager.createQuery(queryStr, UniWithOrWithoutNtu.class);
            query.setParameter(AA_UNI_SFP, aaUniSfp);

            return query.getResultList().stream().findFirst();
        } catch (NoResultException e) {
            logger.debug("No UniWithOrWithoutNtu found for AA UNI SFP: {}", aaUniSfp);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding UniWithOrWithoutNtu by AA UNI SFP: {}", aaUniSfp, e);
            return Optional.empty();
        }
    }
}