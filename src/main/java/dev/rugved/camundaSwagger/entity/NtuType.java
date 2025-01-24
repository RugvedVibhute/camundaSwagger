package dev.rugved.camundaSwagger.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ntu_type", schema = "public")
public class NtuType {

    @Id
    private Long sr_no;

    @Column(name = "ntu_size", nullable = false)
    private String ntuSize;

    @Column(name = "ntu_type", nullable = false)
    private String ntuType;

    // Getters and Setters
    public Long getId() {
        return sr_no;
    }

    public void setId(Long id) {
        this.sr_no = id;
    }

    public String getNtuSize() {
        return ntuSize;
    }

    public void setNtuSize(String ntuSize) {
        this.ntuSize = ntuSize;
    }

    public String getNtuType() {
        return ntuType;
    }

    public void setNtuType(String ntuType) {
        this.ntuType = ntuType;
    }
}
