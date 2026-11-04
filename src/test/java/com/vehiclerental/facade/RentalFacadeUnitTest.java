package com.vehiclerental.facade;

import com.vehiclerental.contract.ExtraType;
import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.RentalEventType;
import com.vehiclerental.contract.Vehicle;
import com.vehiclerental.contract.VehicleType;
import com.vehiclerental.observer.CustomerNotifier;
import com.vehiclerental.observer.FleetAnalyticsListener;
import com.vehiclerental.strategy.HourlyPricing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RentalFacadeUnitTest {

    private RentalFacade facade;
    private CustomerNotifier customerNotifier;
    private FleetAnalyticsListener analyticsListener;

    @BeforeEach
    void setUp() {
        facade = new RentalFacade();
        customerNotifier = new CustomerNotifier("Aigerim");
        analyticsListener = new FleetAnalyticsListener();
        facade.addListener(customerNotifier);
        facade.addListener(analyticsListener);
    }

    @Test
    void shouldRegisterVehicleAndNotifyAnalyticsListener() {
        Vehicle car = facade.registerVehicle(VehicleType.CAR, "CAR-101", "Toyota Camry 70", 8.0);

        assertNotNull(car);
        assertEquals("CAR-101", car.getId());
        assertTrue(car.isAvailable());
        assertEquals(1, facade.getAvailableVehicles().size());
        assertEquals(1, analyticsListener.getEventCount(RentalEventType.VEHICLE_REGISTERED));
    }

    @Test
    void shouldApplyPriceDropAndNotifySubscribedCustomers() {
        facade.registerVehicle(VehicleType.VAN, "VAN-303", "Mercedes Sprinter", 14.0);

        facade.applyPriceDrop("VAN-303", 11.5);

        assertEquals(1, analyticsListener.getEventCount(RentalEventType.PRICE_DROPPED));
        assertEquals(1, customerNotifier.getReceivedNotifications().size());
        assertTrue(customerNotifier.getReceivedNotifications().get(0).contains("11.50"));
    }

    @Test
    void shouldRentAndReturnVehicleUpdatingAvailabilityAndFiringEvents() {
        facade.registerVehicle(VehicleType.BIKE, "BIKE-202", "Xiaomi Pro E-Bike", 3.0);
        PricingStrategy hourly = new HourlyPricing();

        Rental rental = facade.rentVehicle(
                "R-101",
                "Sanzhar",
                "BIKE-202",
                5,
                hourly,
                List.of(ExtraType.GPS),
                null,
                null
        );

        assertNotNull(rental);
        assertEquals(0, facade.getAvailableVehicles().size());
        assertEquals(1, facade.getActiveRentals().size());
        assertEquals(1, analyticsListener.getEventCount(RentalEventType.VEHICLE_RENTED));

        facade.returnVehicle("R-101");

        assertEquals(1, facade.getAvailableVehicles().size());
        assertEquals(0, facade.getActiveRentals().size());
        assertEquals(1, analyticsListener.getEventCount(RentalEventType.VEHICLE_AVAILABLE));
        assertEquals(1, customerNotifier.getReceivedNotifications().size());
    }
}