package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.config.QueryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static dev.rugved.camundaSwagger.util.Constants.*;

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
        String queryStr = queryConfig.getNtuNniSfpOrAaSfp().get(FIND_NTU_NNI_SFP).toString();

        TypedQuery<String> query = entityManager.createQuery(queryStr, String.class);
        query.setParameter(NTU_SIZE, ntuSize);
        query.setParameter(DISTANCE_RANGES, "%" + distanceRanges + "%");
        query.setParameter(VENDOR_TYPE, "%" + vendorType + "%");

        return query.getResultList().stream().findFirst().orElse(null);
    }

    public String findAaSfp(String ntuSize, String distanceRanges, String vendorType) {
        String queryStr = queryConfig.getNtuNniSfpOrAaSfp().get(FIND_AA_SFP).toString();

        TypedQuery<String> query = entityManager.createQuery(queryStr, String.class);
        query.setParameter(NTU_SIZE, ntuSize);
        query.setParameter(DISTANCE_RANGES, "%" + distanceRanges + "%");
        query.setParameter(VENDOR_TYPE, "%" + vendorType + "%");

        return query.getResultList().stream().findFirst().orElse(null);
    }
}
