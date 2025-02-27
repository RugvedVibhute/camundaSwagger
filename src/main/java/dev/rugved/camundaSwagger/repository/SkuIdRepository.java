package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.SkuId;
import dev.rugved.camundaSwagger.config.QueryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Repository
public class SkuIdRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public SkuIdRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public Optional<SkuId> findByAaUniSfp(String aaUniSfp) {
        String queryStr = queryConfig.getSkuId().get(FIND_BY_AA_UNI_SFP).toString();

        TypedQuery<SkuId> query = entityManager.createQuery(queryStr, SkuId.class);
        query.setParameter(AA_UNI_SFP, aaUniSfp);

        return query.getResultList().stream().findFirst();
    }
}
