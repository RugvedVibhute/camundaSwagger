package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.ShipToAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipToAddressRepository extends JpaRepository<ShipToAddress, Long> {

    List<ShipToAddress> findByStateOrProvince(String stateOrProvince);
}
