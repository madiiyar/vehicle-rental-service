package com.vehiclerental.strategy;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Vehicle;

public class DailyPricing implements PricingStrategy {

    @Override
    public String getStrategyName() {
        return "Daily Flat-Discount Pricing (15% off per 24h block)";
    }

    @Override
    public double calculatePrice(Vehicle vehicle, int durationHours) {
        if (vehicle == null || durationHours <= 0) {
            throw new IllegalArgumentException("Vehicle cannot be null and duration must be positive");
        }
        double basePrice = vehicle.getBaseHourlyRate() * durationHours;
        double price = (durationHours >= 24) ? basePrice * 0.85 : basePrice;
        return Math.round(price * 100.0) / 100.0;
    }
}