package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.NtuNniSfpOrAaSfp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NtuNniSfpOrAaSfpRepository extends JpaRepository<NtuNniSfpOrAaSfp, Long> {

    @Query("SELECT n.ntuNniSfp FROM NtuNniSfpOrAaSfp n WHERE n.ntuSize = :ntuSize " +
            "AND :distanceRanges LIKE CONCAT('%', n.distanceRanges, '%') " +
            "AND n.vendorType LIKE %:vendorType%")
    String findNtuNniSfp(@Param("ntuSize") String ntuSize,
                         @Param("distanceRanges") String distanceRanges,
                         @Param("vendorType") String vendorType);

    @Query("SELECT n.aaSfp FROM NtuNniSfpOrAaSfp n WHERE n.ntuSize = :ntuSize " +
            "AND :distanceRanges LIKE CONCAT('%', n.distanceRanges, '%') " +
            "AND n.vendorType LIKE %:vendorType%")
    String findAaSfp(@Param("ntuSize") String ntuSize,
                     @Param("distanceRanges") String distanceRanges,
                     @Param("vendorType") String vendorType);
}
