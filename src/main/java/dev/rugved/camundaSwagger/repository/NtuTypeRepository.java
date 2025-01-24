package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.NtuType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NtuTypeRepository extends JpaRepository<NtuType, Long> {
    Optional<NtuType> findByNtuSize(String ntuSize);
}
