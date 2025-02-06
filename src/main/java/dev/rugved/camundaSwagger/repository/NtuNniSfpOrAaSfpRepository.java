package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.config.QueryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class NtuNniSfpOrAaSfpRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public NtuNniSfpOrAaSfpRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public String findNtuNniSfp(String ntuSize, String distanceRanges, String vendorType) {
        String queryStr = queryConfig.getNtuNniSfpOrAaSfp().get("findNtuNniSfp").toString();

        TypedQuery<String> query = entityManager.createQuery(queryStr, String.class);
        query.setParameter("ntuSize", ntuSize);
        query.setParameter("distanceRanges", "%" + distanceRanges + "%");
        query.setParameter("vendorType", "%" + vendorType + "%");

        return query.getResultList().stream().findFirst().orElse(null);
    }

    public String findAaSfp(String ntuSize, String distanceRanges, String vendorType) {
        String queryStr = queryConfig.getNtuNniSfpOrAaSfp().get("findAaSfp").toString();

        TypedQuery<String> query = entityManager.createQuery(queryStr, String.class);
        query.setParameter("ntuSize", ntuSize);
        query.setParameter("distanceRanges", "%" + distanceRanges + "%");
        query.setParameter("vendorType", "%" + vendorType + "%");

        return query.getResultList().stream().findFirst().orElse(null);
    }
}
