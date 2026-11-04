package com.vehiclerental.builder;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.Vehicle;

public class RentalBuilder {
    private String rentalId;
    private String customerName;
    private Vehicle vehicle;
    private int durationHours;
    private PricingStrategy pricingStrategy;
    private String promoCode;
    private String notes;

    public RentalBuilder withRentalId(String rentalId) {
        this.rentalId = rentalId;
        return this;
    }

    public RentalBuilder withCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    public RentalBuilder withVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }

    public RentalBuilder withDurationHours(int durationHours) {
        this.durationHours = durationHours;
        return this;
    }

    public RentalBuilder withPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
        return this;
    }

    public RentalBuilder withPromoCode(String promoCode) {
        this.promoCode = promoCode;
        return this;
    }

    public RentalBuilder withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public Rental build() {
        if (rentalId == null || rentalId.isBlank()) {
            throw new IllegalStateException("Rental ID is required.");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalStateException("Customer name is required.");
        }
        if (vehicle == null) {
            throw new IllegalStateException("Vehicle is required.");
        }
        if (durationHours <= 0) {
            throw new IllegalStateException("Duration must be greater than zero.");
        }
        if (pricingStrategy == null) {
            throw new IllegalStateException("Pricing strategy is required.");
        }
        return new BaseRental(rentalId.trim(), customerName.trim(), vehicle,
                durationHours, pricingStrategy, normalizeOptional(promoCode),
                normalizeOptional(notes));
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
