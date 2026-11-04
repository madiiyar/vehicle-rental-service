package com.vehiclerental.builder;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.Vehicle;
import java.util.Locale;

public class BaseRental implements Rental {
    private final String rentalId;
    private final String customerName;
    private final Vehicle vehicle;
    private final int durationHours;
    private final PricingStrategy pricingStrategy;
    private final String promoCode;
    private final String notes;

    BaseRental(String rentalId, String customerName, Vehicle vehicle,
               int durationHours, PricingStrategy pricingStrategy,
               String promoCode, String notes) {
        this.rentalId = rentalId;
        this.customerName = customerName;
        this.vehicle = vehicle;
        this.durationHours = durationHours;
        this.pricingStrategy = pricingStrategy;
        this.promoCode = promoCode;
        this.notes = notes;
    }

    @Override
    public String getRentalId() {
        return rentalId;
    }

    @Override
    public String getCustomerName() {
        return customerName;
    }

    @Override
    public Vehicle getVehicle() {
        return vehicle;
    }

    @Override
    public int getDurationHours() {
        return durationHours;
    }

    @Override
    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    @Override
    public double calculateTotalPrice() {
        double price = pricingStrategy.calculatePrice(vehicle, durationHours);
        if ("AITU10".equalsIgnoreCase(promoCode)) {
            price = price * 0.90;
        }
        return price;
    }

    @Override
    public String getDescription() {
        String description = String.format(Locale.ROOT,
                "Rental %s | Customer: %s | Vehicle: %s | Duration: %d hours | Pricing: %s",
                rentalId, customerName, vehicle.getModel(), durationHours,
                pricingStrategy.getStrategyName());
        if (promoCode != null && !promoCode.isBlank()) {
            description += " | Promo: " + promoCode;
        }
        if (notes != null && !notes.isBlank()) {
            description += " | Notes: " + notes;
        }
        return description;
    }
}
