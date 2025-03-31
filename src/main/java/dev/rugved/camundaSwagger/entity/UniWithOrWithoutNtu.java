package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a UNI configuration with or without NTU.
 */
@Entity
@Table(name = "\"uni_with_or_without_ntu\"", schema = "public")
public class UniWithOrWithoutNtu {

    @Id
    @Column(name = "sr_no")
    private Long srNo;

    @Column(name = "distance_ranges")
    private String distanceRanges;

    @Column(name = "ntu_required")
    private String ntuRequired;

    @Column(name = "ntu_size")
    private String ntuSize;

    @Column(name = "vendor_type")
    private String vendorType;

    @Column(name = "uni_port_capacity")
    private String uniPortCapacity;

    @Column(name = "uni_interface_type")
    private String uniInterfaceType;

    @Column(name = "aa_uni_sfp")
    private String aaUniSfp;

    // Getters and Setters
    public Long getSrNo() {
        return srNo;
    }

    public void setSrNo(Long srNo) {
        this.srNo = srNo;
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

    @Override
    public String toString() {
        return "UniWithOrWithoutNtu{" +
                "srNo=" + srNo +
                ", distanceRanges='" + distanceRanges + '\'' +
                ", ntuRequired='" + ntuRequired + '\'' +
                ", ntuSize='" + ntuSize + '\'' +
                ", vendorType='" + vendorType + '\'' +
                ", uniPortCapacity='" + uniPortCapacity + '\'' +
                ", uniInterfaceType='" + uniInterfaceType + '\'' +
                ", aaUniSfp='" + aaUniSfp + '\'' +
                '}';
    }
}