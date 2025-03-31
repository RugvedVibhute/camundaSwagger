package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a SKU ID and its associated AA UNI SFP.
 */
@Entity
@Table(name = "sku_id", schema = "public")
public class SkuId {

    @Id
    @Column(name = "sr_no")
    private Long srNo;

    @Column(name = "aa_uni_sfp")
    private String aaUniSfp;

    @Column(name = "aa_uni_sfp_sku_id")
    private String aaUniSfpSkuId;

    // Getters and setters
    public Long getSrNo() {
        return srNo;
    }

    public void setSrNo(Long srNo) {
        this.srNo = srNo;
    }

    public String getAaUniSfp() {
        return aaUniSfp;
    }

    public void setAaUniSfp(String aaUniSfp) {
        this.aaUniSfp = aaUniSfp;
    }

    public String getAaUniSfpSkuId() {
        return aaUniSfpSkuId;
    }

    public void setAaUniSfpSkuId(String aaUniSfpSkuId) {
        this.aaUniSfpSkuId = aaUniSfpSkuId;
    }

    @Override
    public String toString() {
        return "SkuId{" +
                "srNo=" + srNo +
                ", aaUniSfp='" + aaUniSfp + '\'' +
                ", aaUniSfpSkuId='" + aaUniSfpSkuId + '\'' +
                '}';
    }
}