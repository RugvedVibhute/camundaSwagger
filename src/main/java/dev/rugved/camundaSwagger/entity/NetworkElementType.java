package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"network_element_type\"", schema = "public")
public class NetworkElementType {

    @Id
    @Column(name = "sr_no")
    private Long srNo;

    @Column(name = "network_element")
    private String networkElement;

    @Column(name = "vendor_type")
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

