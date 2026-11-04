package com.vehiclerental.decorator;

import com.vehiclerental.contract.Rental;

public class ChildSeatDecorator extends RentalDecorator {

    public ChildSeatDecorator(Rental wrappedRental) {
        super(wrappedRental);
    }

    @Override
    public double calculateTotalPrice() {
        return super.calculateTotalPrice() + 12.0;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + Child Safety Seat ($12.00)";
    }
}