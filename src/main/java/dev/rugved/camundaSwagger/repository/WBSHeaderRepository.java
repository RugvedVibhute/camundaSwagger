package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.WBSHeader;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import dev.rugved.camundaSwagger.config.QueryConfig;

import java.util.Optional;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Repository
public class WBSHeaderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public WBSHeaderRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public Optional<WBSHeader> findByStateOrProvince(String stateOrProvince) {
        String queryStr = queryConfig.getWbsHeader().get(FIND_BY_STATE_OR_PROVINCE).toString();

        TypedQuery<WBSHeader> query = entityManager.createQuery(queryStr, WBSHeader.class);
        query.setParameter(STATE_OR_PROVINCE, stateOrProvince);

        return query.getResultList().stream().findFirst();
    }
}
