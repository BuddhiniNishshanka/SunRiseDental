package com.sunrisedental.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Surgical Billing Strategy: Covers surgical sterilization & consumables surcharge (5%).
 */
public class SurgicalBillingStrategy implements IBillingStrategy {

    private static final BigDecimal SURGICAL_STERILIZATION_SURCHARGE = new BigDecimal("1500.00");

    @Override
    public BillingCalculationResult calculate(BigDecimal consultationFee, BigDecimal treatmentCost, BigDecimal customDiscountRate, BigDecimal customTaxRate) {
        BigDecimal consult = consultationFee != null ? consultationFee : BigDecimal.ZERO;
        BigDecimal treat = treatmentCost != null ? treatmentCost.add(SURGICAL_STERILIZATION_SURCHARGE) : SURGICAL_STERILIZATION_SURCHARGE;
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
        return "Surgical Procedure Plan (Includes Sterilization Surcharge)";
    }
}
