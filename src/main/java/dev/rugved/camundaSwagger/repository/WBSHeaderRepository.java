package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.WBSHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WBSHeaderRepository extends JpaRepository<WBSHeader, Long> {

    @Query("SELECT w FROM WBSHeader w WHERE w.stateOrProvince = :stateOrProvince")
    Optional<WBSHeader> findByStateOrProvince(@Param("stateOrProvince") String stateOrProvince);
}
