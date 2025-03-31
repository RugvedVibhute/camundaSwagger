package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.config.QueryConfig;
import dev.rugved.camundaSwagger.entity.WBSHeader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static dev.rugved.camundaSwagger.util.Constants.FIND_BY_STATE_OR_PROVINCE;
import static dev.rugved.camundaSwagger.util.Constants.STATE_OR_PROVINCE;

@Repository
public class WBSHeaderRepository {

    private static final Logger logger = LoggerFactory.getLogger(WBSHeaderRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public WBSHeaderRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public Optional<WBSHeader> findByStateOrProvince(String stateOrProvince) {
        if (stateOrProvince == null || stateOrProvince.trim().isEmpty()) {
            logger.warn("Empty or null stateOrProvince provided");
            return Optional.empty();
        }

        try {
            String queryStr = queryConfig.getWbsHeader().get(FIND_BY_STATE_OR_PROVINCE);
            if (queryStr == null) {
                logger.error("Query for FIND_BY_STATE_OR_PROVINCE not found in configuration");
                return Optional.empty();
            }

            TypedQuery<WBSHeader> query = entityManager.createQuery(queryStr, WBSHeader.class);
            query.setParameter(STATE_OR_PROVINCE, stateOrProvince);

            logger.debug("Executing query to find WBS Header by state: {}", stateOrProvince);

            return query.getResultList().stream().findFirst();
        } catch (NoResultException e) {
            logger.debug("No WBS Header found for state: {}", stateOrProvince);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding WBS Header for state: {}", stateOrProvince, e);
            return Optional.empty();
        }
    }
}