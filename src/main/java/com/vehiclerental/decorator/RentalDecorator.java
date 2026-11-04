package com.vehiclerental.decorator;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.Vehicle;

public abstract class RentalDecorator implements Rental {
    protected final Rental wrappedRental;

    protected RentalDecorator(Rental wrappedRental) {
        if (wrappedRental == null) {
            throw new IllegalArgumentException("Wrapped rental cannot be null");
        }
        this.wrappedRental = wrappedRental;
    }

    @Override public String getRentalId() { return wrappedRental.getRentalId(); }
    @Override public String getCustomerName() { return wrappedRental.getCustomerName(); }
    @Override public Vehicle getVehicle() { return wrappedRental.getVehicle(); }
    @Override public int getDurationHours() { return wrappedRental.getDurationHours(); }
    @Override public PricingStrategy getPricingStrategy() { return wrappedRental.getPricingStrategy(); }
    @Override public double calculateTotalPrice() { return wrappedRental.calculateTotalPrice(); }
    @Override public String getDescription() { return wrappedRental.getDescription(); }
}