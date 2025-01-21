package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "uni_with_or_without_ntu")
public class UniWithOrWithoutNtu {

    @Id
    private Long srNo;

    private String distanceRanges;
    private String ntuRequired;
    private String ntuSize;
    private String vendorType;
    private String uniPortCapacity;
    private String uniInterfaceType;
    private String aaUniSfp;

    // Getters and Setters


    public Long getId() {
        return srNo;
    }

    public void setId(Long id) {
        this.srNo = id;
    }

    public String getDistanceRanges() {
        return distanceRanges;
    }

    public void setDistanceRanges(String distanceRanges) {
        this.distanceRanges = distanceRanges;
    }

    public String getNtuRequired() {
        return ntuRequired;
    }

    public void setNtuRequired(String ntuRequired) {
        this.ntuRequired = ntuRequired;
    }

    public String getNtuSize() {
        return ntuSize;
    }

    public void setNtuSize(String ntuSize) {
        this.ntuSize = ntuSize;
    }

    public String getVendorType() {
        return vendorType;
    }

    public void setVendorType(String vendorType) {
        this.vendorType = vendorType;
    }

    public String getUniPortCapacity() {
        return uniPortCapacity;
    }

    public void setUniPortCapacity(String uniPortCapacity) {
        this.uniPortCapacity = uniPortCapacity;
    }

    public String getUniInterfaceType() {
        return uniInterfaceType;
    }

    public void setUniInterfaceType(String uniInterfaceType) {
        this.uniInterfaceType = uniInterfaceType;
    }

    public String getAaUniSfp() {
        return aaUniSfp;
    }

    public void setAaUniSfp(String aaUniSfp) {
        this.aaUniSfp = aaUniSfp;
    }
}

