package com.sunrisedental.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Senior Citizen Billing Strategy: Applies an automated 10% senior subsidy plus any custom promotional discount.
 */
public class SeniorDiscountBillingStrategy implements IBillingStrategy {

    private static final BigDecimal SENIOR_BASE_DISCOUNT = new BigDecimal("10.00");

    @Override
    public BillingCalculationResult calculate(BigDecimal consultationFee, BigDecimal treatmentCost, BigDecimal customDiscountRate, BigDecimal customTaxRate) {
        BigDecimal consult = consultationFee != null ? consultationFee : BigDecimal.ZERO;
        BigDecimal treat = treatmentCost != null ? treatmentCost : BigDecimal.ZERO;
        BigDecimal addlDiscount = customDiscountRate != null ? customDiscountRate : BigDecimal.ZERO;
        BigDecimal effectiveDiscountRate = SENIOR_BASE_DISCOUNT.add(addlDiscount);
        BigDecimal txRate = customTaxRate != null ? customTaxRate : BigDecimal.ZERO;

        BigDecimal subtotal = consult.add(treat);
        BigDecimal discountAmount = subtotal.multiply(effectiveDiscountRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = taxableAmount.multiply(txRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = taxableAmount.add(taxAmount);

        return new BillingCalculationResult(
                consult, treat, subtotal, effectiveDiscountRate, discountAmount, txRate, taxAmount, totalAmount, getStrategyName()
        );
    }

    @Override
    public String getStrategyName() {
        return "Senior Citizen Care (10% Base Discount)";
    }
}
