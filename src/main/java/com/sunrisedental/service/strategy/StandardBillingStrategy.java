package com.sunrisedental.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StandardBillingStrategy implements IBillingStrategy {

    @Override
    public BillingCalculationResult calculate(BigDecimal consultationFee, BigDecimal treatmentCost, BigDecimal customDiscountRate, BigDecimal customTaxRate) {
        BigDecimal consult = consultationFee != null ? consultationFee : BigDecimal.ZERO;
        BigDecimal treat = treatmentCost != null ? treatmentCost : BigDecimal.ZERO;
        BigDecimal discRate = customDiscountRate != null ? customDiscountRate : BigDecimal.ZERO;
        BigDecimal txRate = customTaxRate != null ? customTaxRate : BigDecimal.ZERO;

        BigDecimal subtotal = consult.add(treat);
        BigDecimal discountAmount = subtotal.multiply(discRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = taxableAmount.multiply(txRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = taxableAmount.add(taxAmount);

        return new BillingCalculationResult(
                consult, treat, subtotal, discRate, discountAmount, txRate, taxAmount, totalAmount, getStrategyName()
        );
    }

    @Override
    public String getStrategyName() {
        return "Standard Clinic Billing";
    }
}
