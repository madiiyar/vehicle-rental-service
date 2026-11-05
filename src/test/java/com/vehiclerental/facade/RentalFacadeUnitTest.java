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

    @Test
    void shouldReturnPaddedRentalUsingItsReportedId() {
        Vehicle car = facade.registerVehicle(VehicleType.CAR, "C-1", "Camry", 10.0);
        Rental rental = facade.rentVehicle(" R-101 ", "Amir", "C-1", 5,
                new HourlyPricing(), List.of(ExtraType.GPS), null, null);

        assertEquals("R-101", rental.getRentalId());
        facade.returnVehicle(rental.getRentalId());

        assertTrue(car.isAvailable());
        assertTrue(facade.getActiveRentals().isEmpty());
        assertEquals(1, analyticsListener.getEventCount(RentalEventType.VEHICLE_AVAILABLE));
    }

    @Test
    void shouldRejectCanonicalDuplicateOfPaddedRentalId() {
        Vehicle first = facade.registerVehicle(VehicleType.CAR, "C-1", "First", 10.0);
        Vehicle second = facade.registerVehicle(VehicleType.CAR, "C-2", "Second", 10.0);
        Rental original = facade.rentVehicle(" R-101 ", "Amir", "C-1", 5,
                new HourlyPricing(), List.of(), null, null);

        assertThrows(IllegalArgumentException.class, () -> facade.rentVehicle(
                "R-101", "Other", "C-2", 5, new HourlyPricing(), List.of(), null, null));

        assertEquals(1, facade.getActiveRentals().size());
        assertSame(original, facade.getActiveRentals().iterator().next());
        assertFalse(first.isAvailable());
        assertTrue(second.isAvailable());
        assertEquals(1, analyticsListener.getEventCount(RentalEventType.VEHICLE_RENTED));
    }

    @Test
    void shouldRejectWhitespaceVariantsOfExistingRentalId() {
        facade.registerVehicle(VehicleType.CAR, "C-1", "First", 10.0);
        Vehicle second = facade.registerVehicle(VehicleType.CAR, "C-2", "Second", 10.0);
        facade.rentVehicle("R-101", "Amir", "C-1", 5,
                new HourlyPricing(), List.of(), null, null);

        for (String id : new String[]{"R-101", " R-101 ", "  R-101"}) {
            assertThrows(IllegalArgumentException.class, () -> facade.rentVehicle(
                    id, "Other", "C-2", 5, new HourlyPricing(), List.of(), null, null));
        }
        assertEquals(1, facade.getActiveRentals().size());
        assertTrue(second.isAvailable());
    }

    @Test
    void shouldTrimRentalIdSuppliedForReturn() {
        Vehicle car = facade.registerVehicle(VehicleType.CAR, "C-1", "Camry", 10.0);
        facade.rentVehicle("R-101", "Amir", "C-1", 5,
                new HourlyPricing(), List.of(), null, null);

        facade.returnVehicle(" R-101 ");

        assertTrue(car.isAvailable());
        assertTrue(facade.getActiveRentals().isEmpty());
    }

    @Test
    void shouldRejectInvalidRentalIdsWithoutChangingFleetOrRecords() {
        Vehicle car = facade.registerVehicle(VehicleType.CAR, "C-1", "Camry", 10.0);
        for (String id : new String[]{null, "", "   "}) {
            assertThrows(IllegalArgumentException.class, () -> facade.rentVehicle(
                    id, "Amir", "C-1", 5, new HourlyPricing(), List.of(), null, null));
            assertThrows(IllegalArgumentException.class, () -> facade.returnVehicle(id));
        }
        assertTrue(car.isAvailable());
        assertTrue(facade.getActiveRentals().isEmpty());
        assertEquals(0, analyticsListener.getEventCount(RentalEventType.VEHICLE_RENTED));
    }

    @Test
    void shouldRejectDuplicateVehicleIdWithoutReplacingRentedVehicle() {
        Vehicle car = facade.registerVehicle(VehicleType.CAR, "C-1", "Original", 10.0);
        Rental rental = facade.rentVehicle("R-101", "Amir", "C-1", 5,
                new HourlyPricing(), List.of(), null, null);

        assertThrows(IllegalArgumentException.class, () ->
                facade.registerVehicle(VehicleType.CAR, " C-1 ", "Replacement", 20.0));
        assertSame(car, rental.getVehicle());
        assertEquals(0, facade.getAvailableVehicles().size());
        assertEquals(1, analyticsListener.getEventCount(RentalEventType.VEHICLE_REGISTERED));

        facade.returnVehicle("R-101");
        assertSame(car, facade.getAvailableVehicles().get(0));
    }
}
