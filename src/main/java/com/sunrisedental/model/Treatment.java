package com.sunrisedental.model;

import java.math.BigDecimal;

public class Treatment {
    private int id;
    private String treatmentCode;
    private String name;
    private String description;
    private BigDecimal standardFee;
    private String category; // 'GENERAL', 'SURGICAL', 'COSMETIC', 'ORTHODONTIC'

    public Treatment() {}

    public Treatment(int id, String treatmentCode, String name, String description, BigDecimal standardFee, String category) {
        this.id = id;
        this.treatmentCode = treatmentCode;
        this.name = name;
        this.description = description;
        this.standardFee = standardFee;
        this.category = category;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTreatmentCode() { return treatmentCode; }
    public void setTreatmentCode(String treatmentCode) { this.treatmentCode = treatmentCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getStandardFee() { return standardFee; }
    public void setStandardFee(BigDecimal standardFee) { this.standardFee = standardFee; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
