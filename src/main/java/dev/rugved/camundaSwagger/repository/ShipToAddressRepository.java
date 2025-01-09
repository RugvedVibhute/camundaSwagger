package dev.rugved.camundaSwagger.repository;

import dev.rugved.camundaSwagger.entity.ShipToAddress;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipToAddressRepository extends org.springframework.data.jpa.repository.JpaRepository<ShipToAddress, Long> {

    @Query(value = "SELECT ship_to_address_id, ship_to_address_role FROM public.ship_to_address WHERE state_or_province = :stateOrProvince", nativeQuery = true)
    List<Object[]> findAddressesByStateNative(@Param("stateOrProvince") String stateOrProvince);

    @Query(value = """
        SELECT sold_to_address_role, sold_to_address_id, network_site_address_role, network_site_address_id,
               additional_partner_address_role, additional_partner_address_id
        FROM public.other_address
        """, nativeQuery = true)
    List<Object[]> findOtherAddresses();
}
