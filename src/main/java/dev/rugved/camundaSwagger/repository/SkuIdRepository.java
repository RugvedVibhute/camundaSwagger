package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.SkuId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuIdRepository extends JpaRepository<SkuId, Long> {

    @Query("SELECT s FROM SkuId s WHERE s.aaUniSfp = :aaUniSfp")
    SkuId findByAaUniSfp(@Param("aaUniSfp") String aaUniSfp);
}
