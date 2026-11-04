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

/**
 * Facade pattern implementation providing a unified high-level interface
 * over VehicleFactory, RentalBuilder, PricingStrategy, RentalDecorator, and AvailabilityPublisher.
 */
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
        String normalizedId = (vehicle.getId() != null) ? vehicle.getId().trim() : "";
        if (normalizedId.isEmpty()) {
            throw new IllegalArgumentException("Vehicle ID cannot be null or blank");
        }
        if (fleet.containsKey(normalizedId)) {
            throw new IllegalArgumentException("Vehicle with ID already exists in fleet: " + normalizedId);
        }
        fleet.put(normalizedId, vehicle);
        publisher.notifyListeners(
                RentalEventType.VEHICLE_REGISTERED,
                vehicle,
                "Registered vehicle " + normalizedId + " (" + vehicle.getModel() + ")"
        );
    }

    public Vehicle registerVehicle(VehicleType type, String id, String model, double baseHourlyRate) {
        if (type == null) {
            throw new IllegalArgumentException("VehicleType cannot be null");
        }
        String normalizedId = (id != null) ? id.trim() : "";
        if (fleet.containsKey(normalizedId)) {
            throw new IllegalArgumentException("Vehicle with ID already exists in fleet: " + normalizedId);
        }
        VehicleFactory factory = switch (type) {
            case CAR -> new CarFactory();
            case BIKE -> new BikeFactory();
            case VAN -> new VanFactory();
        };
        Vehicle vehicle = factory.orderNewVehicle(normalizedId, model, baseHourlyRate);
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
        String normalizedRentalId = (rentalId != null) ? rentalId.trim() : "";
        if (normalizedRentalId.isEmpty()) {
            throw new IllegalArgumentException("Rental ID cannot be null or blank");
        }
        if (activeRentals.containsKey(normalizedRentalId)) {
            throw new IllegalArgumentException("Active rental with ID already exists: " + normalizedRentalId);
        }

        String normalizedVehicleId = (vehicleId != null) ? vehicleId.trim() : "";
        Vehicle vehicle = fleet.get(normalizedVehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found: " + normalizedVehicleId);
        }
        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Vehicle is currently unavailable: " + normalizedVehicleId);
        }

        Rental rental = new RentalBuilder()
                .withRentalId(normalizedRentalId)
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
        activeRentals.put(normalizedRentalId, rental);

        publisher.notifyListeners(
                RentalEventType.VEHICLE_RENTED,
                vehicle,
                "Rented by " + customerName + " for $"
                        + String.format(Locale.US, "%.2f", rental.calculateTotalPrice())
        );

        return rental;
    }

    public void returnVehicle(String rentalId) {
        String normalizedRentalId = (rentalId != null) ? rentalId.trim() : "";
        Rental rental = activeRentals.remove(normalizedRentalId);
        if (rental == null) {
            throw new IllegalArgumentException("Active rental not found: " + normalizedRentalId);
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
        String normalizedVehicleId = (vehicleId != null) ? vehicleId.trim() : "";
        Vehicle vehicle = fleet.get(normalizedVehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found: " + normalizedVehicleId);
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