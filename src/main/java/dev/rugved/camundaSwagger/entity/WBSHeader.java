package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"WBS_Header\"", schema = "public")
public class WBSHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Sr_No")
    private Long srNo;

    @Column(name = "State_or_Province")
    private String stateOrProvince;

    @Column(name = "Customer_Type")
    private String customerType;

    @Column(name = "Customer_Sub_Type")
    private String customerSubType;

    @Column(name = "WBS_Header")
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
