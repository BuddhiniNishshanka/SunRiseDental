package com.sunrisedental.service.strategy;

import java.math.BigDecimal;

public class BillingCalculationResult {
    private BigDecimal consultationFee;
    private BigDecimal treatmentCost;
    private BigDecimal subtotal;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String strategyApplied;

    public BillingCalculationResult() {}

    public BillingCalculationResult(BigDecimal consultationFee, BigDecimal treatmentCost, BigDecimal subtotal,
                                    BigDecimal discountRate, BigDecimal discountAmount, BigDecimal taxRate,
                                    BigDecimal taxAmount, BigDecimal totalAmount, String strategyApplied) {
        this.consultationFee = consultationFee;
        this.treatmentCost = treatmentCost;
        this.subtotal = subtotal;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.taxRate = taxRate;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.strategyApplied = strategyApplied;
    }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public BigDecimal getTreatmentCost() { return treatmentCost; }
    public void setTreatmentCost(BigDecimal treatmentCost) { this.treatmentCost = treatmentCost; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStrategyApplied() { return strategyApplied; }
    public void setStrategyApplied(String strategyApplied) { this.strategyApplied = strategyApplied; }
}
