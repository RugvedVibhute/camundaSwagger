package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.NetworkElementType;
import dev.rugved.camundaSwagger.config.QueryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Repository
public class NetworkElementTypeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public NetworkElementTypeRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public Optional<NetworkElementType> findByNetworkElement(String networkElement) {
        String queryStr = queryConfig.getNetworkElementType().get("findByNetworkElement").toString();

        TypedQuery<NetworkElementType> query = entityManager.createQuery(queryStr, NetworkElementType.class);
        query.setParameter("networkElement", networkElement);

        return query.getResultList().stream().findFirst();
    }
}
