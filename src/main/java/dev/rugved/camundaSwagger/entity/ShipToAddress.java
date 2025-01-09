package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ship_to_address", schema = "public")
public class ShipToAddress {

    @Id
    @Column(name = "Sr_No")
    private Long srNo;

    @Column(name = "ship_to_address_id")
    private String shipToAddressId;

    @Column(name = "ship_to_address_role")
    private String shipToAddressRole;

    @Column(name = "state_or_province")
    private String stateOrProvince;

    // Getters and Setters
    public String getShipToAddressId() {
        return shipToAddressId;
    }

    public void setShipToAddressId(String shipToAddressId) {
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

