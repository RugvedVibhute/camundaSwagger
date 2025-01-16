package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"wbs_header\"", schema = "public")
public class WBSHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sr_no")
    private Long srNo;

    @Column(name = "state_or_province")
    private String stateOrProvince;

    @Column(name = "customer_type")
    private String customerType;

    @Column(name = "customer_sub_type")
    private String customerSubType;

    @Column(name = "wbs_header")
    private String wbsHeader;

    // Getters and Setters
    public Long getSrNo() {
        return srNo;
    }

    public void setSrNo(Long srNo) {
        this.srNo = srNo;
    }

    public String getStateOrProvince() {
        return stateOrProvince;
    }

    public void setStateOrProvince(String stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getCustomerSubType() {
        return customerSubType;
    }

    public void setCustomerSubType(String customerSubType) {
        this.customerSubType = customerSubType;
    }

    public String getWbsHeader() {
        return wbsHeader;
    }

    public void setWbsHeader(String wbsHeader) {
        this.wbsHeader = wbsHeader;
    }
}
