package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.SkuId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuIdRepository extends JpaRepository<SkuId, Long> {
    SkuId findByAaUniSfp(String aaUniSfp);
}
