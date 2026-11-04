package com.vehiclerental.decorator;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.Vehicle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RentalDecoratorTest {

    private static class DummyRental implements Rental {
        @Override public String getRentalId() { return "R-1"; }
        @Override public String getCustomerName() { return "Tangharyk"; }
        @Override public Vehicle getVehicle() { return null; }
        @Override public int getDurationHours() { return 24; }
        @Override public PricingStrategy getPricingStrategy() { return null; }
        @Override public double calculateTotalPrice() { return 100.0; }
        @Override public String getDescription() { return "Base Rental"; }
    }

    @Test
    void insuranceDecoratorShouldAddDailyFee() {
        Rental base = new DummyRental();
        Rental insured = new InsuranceDecorator(base);
        assertEquals(115.0, insured.calculateTotalPrice(), 0.001);
        assertTrue(insured.getDescription().contains("Full Insurance"));
    }

    @Test
    void gpsAndChildSeatShouldAddFlatFees() {
        Rental base = new DummyRental();
        Rental withGps = new GpsDecorator(base);
        Rental withSeat = new ChildSeatDecorator(base);

        assertEquals(110.0, withGps.calculateTotalPrice(), 0.001);
        assertEquals(112.0, withSeat.calculateTotalPrice(), 0.001);
    }

    @Test
    void stackedDecoratorsShouldAccumulatePricesAndDescriptions() {
        Rental rental = new DummyRental();
        rental = new InsuranceDecorator(rental);
        rental = new GpsDecorator(rental);
        rental = new ChildSeatDecorator(rental);

        assertEquals(137.0, rental.calculateTotalPrice(), 0.001);
        assertEquals("Base Rental + Full Insurance + GPS Navigation ($10.00) + Child Safety Seat ($12.00)", rental.getDescription());
    }

    @Test
    void decoratorShouldThrowExceptionWhenWrappingNull() {
        assertThrows(IllegalArgumentException.class, () -> new InsuranceDecorator(null));
    }
}