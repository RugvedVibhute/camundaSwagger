package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"Network_Element_Type\"", schema = "public")
public class NetworkElementType {

    @Id
    @Column(name = "Sr_No")
    private Long srNo;

    @Column(name = "Network_Element")
    private String networkElement;

    @Column(name = "Vendor_Type")
    private String vendorType;

    // Getters and Setters
    public Long getSrNo() {
        return srNo;
    }

    public void setSrNo(Long srNo) {
        this.srNo = srNo;
    }

    public String getNetworkElement() {
        return networkElement;
    }

    public void setNetworkElement(String networkElement) {
        this.networkElement = networkElement;
    }

    public String getVendorType() {
        return vendorType;
    }

    public void setVendorType(String vendorType) {
        this.vendorType = vendorType;
    }
}

