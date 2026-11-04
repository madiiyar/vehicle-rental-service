package com.vehiclerental.decorator;

import com.vehiclerental.contract.Rental;

public class InsuranceDecorator extends RentalDecorator {

    public InsuranceDecorator(Rental wrappedRental) {
        super(wrappedRental);
    }

    @Override
    public double calculateTotalPrice() {
        int days = (int) Math.ceil(getDurationHours() / 24.0);
        if (days < 1) days = 1;
        return super.calculateTotalPrice() + (days * 15.0);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + Full Insurance";
    }
}