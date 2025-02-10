package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.config.QueryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static dev.rugved.camundaSwagger.util.Constants.*;

@Repository
public class UniWithOrWithoutNtuRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final QueryConfig queryConfig;

    @Autowired
    public UniWithOrWithoutNtuRepository(QueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public String findAaUniSfp(String distanceRanges, String ntuRequired, String ntuSize, String vendorType, String uniPortCapacity, String uniInterfaceType) {
        String queryStr = queryConfig.getUniWithOrWithoutNtu().get(FIND_AA_UNI_SFP).toString();

        TypedQuery<String> query = entityManager.createQuery(queryStr, String.class);
        query.setParameter(DISTANCE_RANGES, distanceRanges);
        query.setParameter(NTU_REQUIRED, ntuRequired);
        query.setParameter(NTU_SIZE, ntuSize);
        query.setParameter(VENDOR_TYPE, "%" + vendorType + "%"); // Using LIKE operator
        query.setParameter(UNI_PORT_CAPACITY, uniPortCapacity);
        query.setParameter(UNI_INTERFACE_TYPE, uniInterfaceType);

        return query.getSingleResult();
    }
}
