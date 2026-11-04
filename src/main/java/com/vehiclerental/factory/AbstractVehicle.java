package com.vehiclerental.factory;

import com.vehiclerental.contract.Vehicle;
import com.vehiclerental.contract.VehicleType;

/**
 * Stores common vehicle identity, hourly rate and availability. Concrete vehicles supply their specifications.
 */
public abstract class AbstractVehicle implements Vehicle {
    private final String id;
    private final String model;
    private final VehicleType type;
    private double baseHourlyRate;
    private boolean available = true;

    /**
     * Constructs AbstractVehicle with the supplied values.
     * @param id unique vehicle identifier
     * @param model vehicle model
     * @param type vehicle category
     * @param baseHourlyRate finite positive hourly price in USD
     * @throws IllegalArgumentException if vehicle identity, type or hourly rate is invalid
     */
    protected AbstractVehicle(String id, String model, VehicleType type,
                              double baseHourlyRate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID must not be blank.");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Vehicle model must not be blank.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type must not be null.");
        }
        this.id = id.trim();
        this.model = model.trim();
        this.type = type;
        setBaseHourlyRate(baseHourlyRate);
    }

    /**
     * Returns the vehicle identifier.
     * @return vehicle identifier
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Returns the vehicle model.
     * @return vehicle model
     */
    @Override
    public String getModel() {
        return model;
    }

    /**
     * Returns the vehicle category.
     * @return vehicle category
     */
    @Override
    public VehicleType getType() {
        return type;
    }

    /**
     * Returns the current hourly price in USD.
     * @return current hourly price
     */
    @Override
    public double getBaseHourlyRate() {
        return baseHourlyRate;
    }

    /**
     * Updates the hourly price after validating that it is finite and positive.
     * @param newRate new finite positive hourly price in USD
     * @throws IllegalArgumentException if the rate is not finite and positive
     */
    @Override
    public void setBaseHourlyRate(double newRate) {
        if (!Double.isFinite(newRate) || newRate <= 0) {
            throw new IllegalArgumentException("Hourly rate must be a finite positive number.");
        }
        this.baseHourlyRate = newRate;
    }

    /**
     * Reports whether the vehicle is available for rental.
     * @return true when available
     */
    @Override
    public boolean isAvailable() {
        return available;
    }

    /**
     * Updates availability; rental lifecycle decisions belong to the facade.
     * @param available whether the vehicle can be rented
     */
    @Override
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Returns the concrete vehicle specifications.
     * @return human-readable vehicle specifications
     */
    @Override
    public abstract String getSpecs();
}