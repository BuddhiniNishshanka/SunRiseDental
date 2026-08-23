package com.sunrisedental.factory;

import com.sunrisedental.service.strategy.IBillingStrategy;
import com.sunrisedental.service.strategy.SeniorDiscountBillingStrategy;
import com.sunrisedental.service.strategy.StandardBillingStrategy;
import com.sunrisedental.service.strategy.SurgicalBillingStrategy;

/**
 * Factory for creating Billing Strategies dynamically.
 */
public class BillingCalculatorFactory {

    public static IBillingStrategy getStrategy(String treatmentCategory, boolean isSeniorCitizen) {
        if (isSeniorCitizen) {
            return new SeniorDiscountBillingStrategy();
        }
        if (treatmentCategory != null && treatmentCategory.equalsIgnoreCase("SURGICAL")) {
            return new SurgicalBillingStrategy();
        }
        return new StandardBillingStrategy();
    }
}
