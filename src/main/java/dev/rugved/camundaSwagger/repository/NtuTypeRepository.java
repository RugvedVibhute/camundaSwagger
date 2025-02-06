package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.NtuType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NtuTypeRepository extends JpaRepository<NtuType, Long> {

    @Query("SELECT n FROM NtuType n WHERE n.ntuSize = :ntuSize")
    Optional<NtuType> findByNtuSize(@Param("ntuSize") String ntuSize);
}
