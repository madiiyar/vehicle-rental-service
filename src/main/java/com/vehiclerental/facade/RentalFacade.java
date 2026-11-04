package com.vehiclerental.facade;

import com.vehiclerental.builder.RentalBuilder;
import com.vehiclerental.contract.ExtraType;
import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.RentalEventType;
import com.vehiclerental.contract.RentalListener;
import com.vehiclerental.contract.Vehicle;
import com.vehiclerental.contract.VehicleType;
import com.vehiclerental.decorator.ChildSeatDecorator;
import com.vehiclerental.decorator.GpsDecorator;
import com.vehiclerental.decorator.InsuranceDecorator;
import com.vehiclerental.factory.BikeFactory;
import com.vehiclerental.factory.CarFactory;
import com.vehiclerental.factory.VanFactory;
import com.vehiclerental.factory.VehicleFactory;
import com.vehiclerental.observer.AvailabilityPublisher;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RentalFacade {
    private final Map<String, Vehicle> fleet = new LinkedHashMap<>();
    private final Map<String, Rental> activeRentals = new LinkedHashMap<>();
    private final AvailabilityPublisher publisher = new AvailabilityPublisher();

    public void addListener(RentalListener listener) {
        publisher.subscribe(listener);
    }

    public void removeListener(RentalListener listener) {
        publisher.unsubscribe(listener);
    }

    public void registerVehicleInstance(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        // ИСПРАВЛЕНИЕ 2: Проверка на дубликат ID машины во флоте
        if (fleet.containsKey(vehicle.getId())) {
            throw new IllegalArgumentException("Vehicle with ID already exists in fleet: " + vehicle.getId());
        }
        fleet.put(vehicle.getId(), vehicle);
        publisher.notifyListeners(
                RentalEventType.VEHICLE_REGISTERED,
                vehicle,
                "Registered vehicle " + vehicle.getId() + " (" + vehicle.getModel() + ")"
        );
    }

    public Vehicle registerVehicle(VehicleType type, String id, String model, double baseHourlyRate) {
        if (type == null) {
            throw new IllegalArgumentException("VehicleType cannot be null");
        }
        VehicleFactory factory = switch (type) {
            case CAR -> new CarFactory();
            case BIKE -> new BikeFactory();
            case VAN -> new VanFactory();
        };
        Vehicle vehicle = factory.orderNewVehicle(id, model, baseHourlyRate);
        registerVehicleInstance(vehicle);
        return vehicle;
    }

    public Rental rentVehicle(
            String rentalId,
            String customerName,
            String vehicleId,
            int durationHours,
            PricingStrategy strategy,
            List<ExtraType> extras,
            String promoCode,
            String notes
    ) {
        // ИСПРАВЛЕНИЕ 1: Проверка на дубликат ID активной аренды
        if (activeRentals.containsKey(rentalId)) {
            throw new IllegalArgumentException("Active rental with ID already exists: " + rentalId);
        }

        Vehicle vehicle = fleet.get(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found: " + vehicleId);
        }
        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Vehicle is currently unavailable: " + vehicleId);
        }

        Rental rental = new RentalBuilder()
                .withRentalId(rentalId)
                .withCustomerName(customerName)
                .withVehicle(vehicle)
                .withDurationHours(durationHours)
                .withPricingStrategy(strategy)
                .withPromoCode(promoCode)
                .withNotes(notes)
                .build();

        if (extras != null) {
            for (ExtraType extra : extras) {
                if (extra != null) {
                    rental = switch (extra) {
                        case INSURANCE -> new InsuranceDecorator(rental);
                        case GPS -> new GpsDecorator(rental);
                        case CHILD_SEAT -> new ChildSeatDecorator(rental);
                    };
                }
            }
        }

        vehicle.setAvailable(false);
        activeRentals.put(rentalId, rental);

        publisher.notifyListeners(
                RentalEventType.VEHICLE_RENTED,
                vehicle,
                "Rented by " + customerName + " for $"
                        + String.format(Locale.US, "%.2f", rental.calculateTotalPrice())
        );

        return rental;
    }

    public void returnVehicle(String rentalId) {
        Rental rental = activeRentals.remove(rentalId);
        if (rental == null) {
            throw new IllegalArgumentException("Active rental not found: " + rentalId);
        }
        Vehicle vehicle = rental.getVehicle();
        vehicle.setAvailable(true);
        publisher.notifyListeners(
                RentalEventType.VEHICLE_AVAILABLE,
                vehicle,
                "Vehicle " + vehicle.getId() + " is now available for booking!"
        );
    }

    public void applyPriceDrop(String vehicleId, double newHourlyRate) {
        Vehicle vehicle = fleet.get(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found: " + vehicleId);
        }
        if (newHourlyRate <= 0) {
            throw new IllegalArgumentException("New hourly rate must be positive");
        }
        vehicle.setBaseHourlyRate(newHourlyRate);
        publisher.notifyListeners(
                RentalEventType.PRICE_DROPPED,
                vehicle,
                "Hourly rate dropped to $" + String.format(Locale.US, "%.2f", newHourlyRate) + "/h!"
        );
    }

    public List<Vehicle> getAvailableVehicles() {
        return fleet.values().stream()
                .filter(Vehicle::isAvailable)
                .toList();
    }

    public Collection<Rental> getActiveRentals() {
        return Collections.unmodifiableCollection(activeRentals.values());
    }
}