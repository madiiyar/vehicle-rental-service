package com.vehiclerental.strategy;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Vehicle;

public class WeeklyNightPricing implements PricingStrategy {

    @Override
    public String getStrategyName() {
        return "Weekly / Long-Term Loyalty Pricing (30% off 7+ days, 10% off 12h+)";
    }

    @Override
    public double calculatePrice(Vehicle vehicle, int durationHours) {
        if (vehicle == null || durationHours <= 0) {
            throw new IllegalArgumentException("Vehicle cannot be null and duration must be positive");
        }
        double basePrice = vehicle.getBaseHourlyRate() * durationHours;
        double price;
        if (durationHours >= 168) {
            price = basePrice * 0.70;
        } else if (durationHours >= 12) {
            price = basePrice * 0.90;
        } else {
            price = basePrice;
        }
        return Math.round(price * 100.0) / 100.0;
    }
}