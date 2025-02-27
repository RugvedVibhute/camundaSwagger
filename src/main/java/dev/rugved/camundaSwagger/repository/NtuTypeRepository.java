package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.NtuType;
import dev.rugved.camundaSwagger.config.QueryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Repository
public class NtuTypeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public NtuTypeRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public Optional<NtuType> findByNtuSize(String ntuSize) {
        String queryStr = queryConfig.getNtuType().get(FIND_BY_NTU_SIZE).toString();

        TypedQuery<NtuType> query = entityManager.createQuery(queryStr, NtuType.class);
        query.setParameter(NTU_SIZE, ntuSize);

        return query.getResultList().stream().findFirst();
    }
}
