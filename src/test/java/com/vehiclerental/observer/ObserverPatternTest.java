package com.vehiclerental.observer;

import com.vehiclerental.contract.RentalEventType;
import com.vehiclerental.contract.Vehicle;
import com.vehiclerental.contract.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObserverPatternTest {

    private AvailabilityPublisher publisher;
    private Vehicle stubVehicle;

    @BeforeEach
    void setUp() {
        publisher = new AvailabilityPublisher();
        stubVehicle = new TestVehicleStub("CAR-101", "Toyota Camry 70", VehicleType.CAR, 8.0);
    }

    @Test
    void publisherShouldNotifyAllSubscribedListenersOnVehicleAvailable() {
        CustomerNotifier aigerim = new CustomerNotifier("Aigerim");
        CustomerNotifier nurlan = new CustomerNotifier("Nurlan");
        FleetAnalyticsListener analytics = new FleetAnalyticsListener();

        publisher.subscribe(aigerim);
        publisher.subscribe(nurlan);
        publisher.subscribe(analytics);

        publisher.notifyListeners(
                RentalEventType.VEHICLE_AVAILABLE,
                stubVehicle,
                "Vehicle CAR-101 is now available for booking!"
        );

        assertEquals(1, aigerim.getReceivedNotifications().size());
        assertEquals(1, nurlan.getReceivedNotifications().size());
        assertEquals(1, analytics.getEventCount(RentalEventType.VEHICLE_AVAILABLE));
    }

    @Test
    void unsubscribedListenerShouldStopReceivingNotifications() {
        CustomerNotifier aigerim = new CustomerNotifier("Aigerim");
        publisher.subscribe(aigerim);

        publisher.notifyListeners(RentalEventType.PRICE_DROPPED, stubVehicle, "Rate dropped to $6.50/h");
        assertEquals(1, aigerim.getReceivedNotifications().size());

        publisher.unsubscribe(aigerim);
        publisher.notifyListeners(RentalEventType.VEHICLE_AVAILABLE, stubVehicle, "Now available!");

        assertEquals(1, aigerim.getReceivedNotifications().size());
    }

    @Test
    void customerNotifierShouldRecordFormattedMessages() {
        CustomerNotifier aigerim = new CustomerNotifier("Aigerim");
        publisher.subscribe(aigerim);

        publisher.notifyListeners(
                RentalEventType.PRICE_DROPPED,
                stubVehicle,
                "Hourly rate dropped to $6.50/h!"
        );

        String expected = "[SMS to Aigerim] Event PRICE_DROPPED for Toyota Camry 70: Hourly rate dropped to $6.50/h!";
        assertEquals(expected, aigerim.getReceivedNotifications().get(0));
    }

    @Test
    void fleetAnalyticsListenerShouldAccuratelyCountEventsByType() {
        FleetAnalyticsListener analytics = new FleetAnalyticsListener();
        publisher.subscribe(analytics);

        publisher.notifyListeners(RentalEventType.VEHICLE_REGISTERED, stubVehicle, "Registered");
        publisher.notifyListeners(RentalEventType.VEHICLE_REGISTERED, stubVehicle, "Registered 2");
        publisher.notifyListeners(RentalEventType.VEHICLE_RENTED, stubVehicle, "Rented");

        assertEquals(2, analytics.getEventCount(RentalEventType.VEHICLE_REGISTERED));
        assertEquals(1, analytics.getEventCount(RentalEventType.VEHICLE_RENTED));
        assertEquals(0, analytics.getEventCount(RentalEventType.VEHICLE_AVAILABLE));
        assertEquals(0, analytics.getEventCount(RentalEventType.PRICE_DROPPED));
    }

    private static class TestVehicleStub implements Vehicle {
        private final String id;
        private final String model;
        private final VehicleType type;
        private double baseHourlyRate;
        private boolean available = true;

        TestVehicleStub(String id, String model, VehicleType type, double baseHourlyRate) {
            this.id = id;
            this.model = model;
            this.type = type;
            this.baseHourlyRate = baseHourlyRate;
        }

        @Override public String getId() { return id; }
        @Override public String getModel() { return model; }
        @Override public VehicleType getType() { return type; }
        @Override public double getBaseHourlyRate() { return baseHourlyRate; }
        @Override public void setBaseHourlyRate(double newRate) { this.baseHourlyRate = newRate; }
        @Override public boolean isAvailable() { return available; }
        @Override public void setAvailable(boolean available) { this.available = available; }
        @Override public String getSpecs() { return "StubSpecs"; }
    }
}