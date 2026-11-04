package com.vehiclerental.strategy;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Vehicle;

public class HourlyPricing implements PricingStrategy {

    @Override
    public String getStrategyName() {
        return "Hourly Standard Pricing";
    }

    @Override
    public double calculatePrice(Vehicle vehicle, int durationHours) {
        if (vehicle == null || durationHours <= 0) {
            throw new IllegalArgumentException("Vehicle cannot be null and duration must be positive");
        }
        double price = vehicle.getBaseHourlyRate() * durationHours;
        return Math.round(price * 100.0) / 100.0;
    }
}