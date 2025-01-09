package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ship_to_address", schema = "public")
public class ShipToAddress {

    @Column(name = "ship_to_address_id")
    private Long shipToAddressId;

    @Column(name = "ship_to_address_role")
    private String shipToAddressRole;

    @Column(name = "state_or_province")
    private String stateOrProvince;

    // Getters and Setters
    public Long getShipToAddressId() {
        return shipToAddressId;
    }

    public void setShipToAddressId(Long shipToAddressId) {
        this.shipToAddressId = shipToAddressId;
    }

    public String getShipToAddressRole() {
        return shipToAddressRole;
    }

    public void setShipToAddressRole(String shipToAddressRole) {
        this.shipToAddressRole = shipToAddressRole;
    }

    public String getStateOrProvince() {
        return stateOrProvince;
    }

    public void setStateOrProvince(String stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }
}

