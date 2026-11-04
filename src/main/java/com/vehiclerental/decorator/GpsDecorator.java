package com.vehiclerental.decorator;

import com.vehiclerental.contract.Rental;

public class GpsDecorator extends RentalDecorator {

    public GpsDecorator(Rental wrappedRental) {
        super(wrappedRental);
    }

    @Override
    public double calculateTotalPrice() {
        return super.calculateTotalPrice() + 10.0;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + GPS Navigation ($10.00)";
    }
}