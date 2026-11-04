package com.vehiclerental.strategy;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Vehicle;
import com.vehiclerental.contract.VehicleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PricingStrategyTest {

    private static class TestVehicle implements Vehicle {
        private final double rate;
        TestVehicle(double rate) { this.rate = rate; }
        @Override public String getId() { return "TEST-1"; }
        @Override public String getModel() { return "Test Model"; }
        @Override public VehicleType getType() { return VehicleType.CAR; }
        @Override public double getBaseHourlyRate() { return rate; }
        @Override public void setBaseHourlyRate(double newRate) {}
        @Override public boolean isAvailable() { return true; }
        @Override public void setAvailable(boolean available) {}
        @Override public String getSpecs() { return "Test Specs"; }
    }

    @Test
    void hourlyPricingShouldMultiplyBaseRateByHours() {
        PricingStrategy strategy = new HourlyPricing();
        Vehicle vehicle = new TestVehicle(10.0);
        assertEquals(50.0, strategy.calculatePrice(vehicle, 5), 0.001);
    }

    @Test
    void dailyPricingShouldApply15PercentDiscountFor24OrMoreHours() {
        PricingStrategy strategy = new DailyPricing();
        Vehicle vehicle = new TestVehicle(10.0);
        assertEquals(204.0, strategy.calculatePrice(vehicle, 24), 0.001);
    }

    @Test
    void weeklyNightPricingShouldApplyDiscountsCorrectly() {
        PricingStrategy strategy = new WeeklyNightPricing();
        Vehicle vehicle = new TestVehicle(10.0);
        assertEquals(108.0, strategy.calculatePrice(vehicle, 12), 0.001);
        assertEquals(1176.0, strategy.calculatePrice(vehicle, 168), 0.001);
    }

    @Test
    void strategiesShouldThrowExceptionForZeroOrNegativeDuration() {
        PricingStrategy strategy = new HourlyPricing();
        Vehicle vehicle = new TestVehicle(10.0);
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrice(vehicle, 0));
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrice(null, 5));
    }
}