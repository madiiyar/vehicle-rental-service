package com.vehiclerental.integration;

import com.vehiclerental.builder.BaseRental;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.Vehicle;
import com.vehiclerental.contract.VehicleType;
import com.vehiclerental.facade.RentalFacade;
import com.vehiclerental.factory.Bike;
import com.vehiclerental.factory.Car;
import com.vehiclerental.factory.Van;
import com.vehiclerental.strategy.HourlyPricing;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FactoryBuilderIntegrationTest {
    private static final double DELTA = 0.000001;

    @Test
    void shouldCreateAllVehicleTypesThroughFacadeAndFactories() {
        RentalFacade facade = new RentalFacade();

        Vehicle car = facade.registerVehicle(
                VehicleType.CAR, "CAR-101", "Toyota Camry", 10.0);
        Vehicle bike = facade.registerVehicle(
                VehicleType.BIKE, "BIKE-101", "City Bike", 3.0);
        Vehicle van = facade.registerVehicle(
                VehicleType.VAN, "VAN-101", "Ford Transit", 15.0);

        assertInstanceOf(Car.class, car);
        assertInstanceOf(Bike.class, bike);
        assertInstanceOf(Van.class, van);
        assertEquals(VehicleType.CAR, car.getType());
        assertEquals(VehicleType.BIKE, bike.getType());
        assertEquals(VehicleType.VAN, van.getType());
        assertEquals("Car [5 seats, Automatic]", car.getSpecs());
        assertEquals("Bike [Electric, Helmet Included]", bike.getSpecs());
        assertEquals("Van [12.5 m3 cargo]", van.getSpecs());
        assertEquals(3, facade.getAvailableVehicles().size());
        assertTrue(car.isAvailable());
        assertTrue(bike.isAvailable());
        assertTrue(van.isAvailable());
    }

    @Test
    void shouldBuildRentalThroughFacadeWithPromoCodeAndNotes() {
        RentalFacade facade = new RentalFacade();
        Vehicle car = facade.registerVehicle(
                VehicleType.CAR, "CAR-101", "Toyota Camry", 10.0);
        HourlyPricing strategy = new HourlyPricing();

        Rental rental = facade.rentVehicle(
                "RENT-101", "Amir", "CAR-101", 5, strategy,
                List.of(), "AITU10", "Pickup at 10:00");

        assertInstanceOf(BaseRental.class, rental);
        assertEquals("RENT-101", rental.getRentalId());
        assertEquals("Amir", rental.getCustomerName());
        assertSame(car, rental.getVehicle());
        assertEquals(5, rental.getDurationHours());
        assertSame(strategy, rental.getPricingStrategy());
        assertEquals(45.0, rental.calculateTotalPrice(), DELTA);
        assertTrue(rental.getDescription().contains("Promo: AITU10"));
        assertTrue(rental.getDescription().contains("Notes: Pickup at 10:00"));
        assertFalse(car.isAvailable());
        assertEquals(1, facade.getActiveRentals().size());
        assertSame(rental, facade.getActiveRentals().iterator().next());
    }

    @Test
    void shouldRejectAlreadyRentedOrUnknownVehicleWithoutChangingRecords() {
        RentalFacade facade = new RentalFacade();
        Vehicle car = facade.registerVehicle(
                VehicleType.CAR, "CAR-101", "Toyota Camry", 10.0);
        HourlyPricing strategy = new HourlyPricing();
        Rental original = facade.rentVehicle(
                "RENT-101", "Amir", "CAR-101", 5, strategy,
                List.of(), null, null);

        // Уже арендованная машина -> IllegalStateException
        assertThrows(IllegalStateException.class, () -> facade.rentVehicle(
                "RENT-102", "Other customer", "CAR-101", 2, strategy,
                List.of(), null, null));

        // Несуществующая машина -> IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> facade.rentVehicle(
                "RENT-103", "Other customer", "MISSING", 2, strategy,
                List.of(), null, null));

        assertFalse(car.isAvailable());
        assertEquals(1, facade.getActiveRentals().size());
        assertSame(original, facade.getActiveRentals().iterator().next());
        assertEquals(50.0, original.calculateTotalPrice(), DELTA);

        facade.returnVehicle(original.getRentalId());
        assertTrue(car.isAvailable());
        assertTrue(facade.getActiveRentals().isEmpty());
    }
}
