package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.UniWithOrWithoutNtu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UniWithOrWithoutNtuRepository extends JpaRepository<UniWithOrWithoutNtu, Long> {

    @Query("SELECT u.aaUniSfp FROM UniWithOrWithoutNtu u " +
            "WHERE :distanceRanges LIKE CONCAT('%', u.distanceRanges, '%') " +
            "AND u.ntuRequired = :ntuRequired " +
            "AND u.ntuSize = :ntuSize " +
            "AND u.vendorType LIKE CONCAT('%', :vendorType, '%') " +  // Use LIKE for vendorType
            "AND u.uniPortCapacity = :uniPortCapacity " +
            "AND u.uniInterfaceType = :uniInterfaceType")
    String findAaUniSfp(@Param("distanceRanges") String distanceRanges,
                        @Param("ntuRequired") String ntuRequired,
                        @Param("ntuSize") String ntuSize,
                        @Param("vendorType") String vendorType,
                        @Param("uniPortCapacity") String uniPortCapacity,
                        @Param("uniInterfaceType") String uniInterfaceType);
}
