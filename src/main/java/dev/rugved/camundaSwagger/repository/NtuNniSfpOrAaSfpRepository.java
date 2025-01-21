package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.NtuNniSfpOrAaSfp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NtuNniSfpOrAaSfpRepository extends JpaRepository<NtuNniSfpOrAaSfp, Long> {

    @Query("SELECT n FROM NtuNniSfpOrAaSfp n WHERE n.ntuSize = :ntuSize " +
            "AND n.distanceRanges = :distanceRanges " +
            "AND n.vendorType LIKE %:vendorType%")
    List<NtuNniSfpOrAaSfp> findMatchingData(String ntuSize, String distanceRanges, String vendorType);
}
