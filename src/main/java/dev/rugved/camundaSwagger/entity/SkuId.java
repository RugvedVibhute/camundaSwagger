package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sku_id", schema = "public")
public class SkuId {

    @Id
    private Long srNo;

    private String aaUniSfp;

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
}
