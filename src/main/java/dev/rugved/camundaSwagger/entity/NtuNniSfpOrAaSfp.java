package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"ntu_nni_sfp_or_aa_sfp\"", schema = "public")
public class NtuNniSfpOrAaSfp {

    @Id
    @Column(name = "\"sr_no\"")
    private Long srNo;

    @Column(name = "\"technology\"")
    private String technology;

    @Column(name = "\"ntu_nni_sfp\"")
    private String ntuNniSfp;

    @Column(name = "\"aa_sfp\"")
    private String aaSfp;

    @Column(name = "\"ntu_size\"")
    private String ntuSize;

    @Column(name = "\"vendor_type\"")
    private String vendorType;

    @Column(name = "\"distance_ranges\"")
    private String distanceRanges;

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public Long getSrNo() {
        return srNo;
    }

    public void setSrNo(Long srNo) {
        this.srNo = srNo;
    }

    public String getNtuNniSfp() {
        return ntuNniSfp;
    }

    public void setNtuNniSfp(String ntuNniSfp) {
        this.ntuNniSfp = ntuNniSfp;
    }

    public String getAaSfp() {
        return aaSfp;
    }

    public void setAaSfp(String aaSfp) {
        this.aaSfp = aaSfp;
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

    public String getDistanceRanges() {
        return distanceRanges;
    }

    public void setDistanceRanges(String distanceRanges) {
        this.distanceRanges = distanceRanges;
    }
}
