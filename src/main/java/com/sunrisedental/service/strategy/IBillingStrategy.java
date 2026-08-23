package com.sunrisedental.service.strategy;

import java.math.BigDecimal;

/**
 * Strategy Design Pattern Interface for Dental Billing Calculations.
 * Permits dynamic selection of fee calculations, tax handling, and discount structures.
 */
public interface IBillingStrategy {
    BillingCalculationResult calculate(BigDecimal consultationFee, BigDecimal treatmentCost, BigDecimal customDiscountRate, BigDecimal customTaxRate);
    String getStrategyName();
}
