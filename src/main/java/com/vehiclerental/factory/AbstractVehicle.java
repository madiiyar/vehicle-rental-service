package com.vehiclerental.factory;

import com.vehiclerental.contract.Vehicle;
import com.vehiclerental.contract.VehicleType;

public abstract class AbstractVehicle implements Vehicle {
    private final String id;
    private final String model;
    private final VehicleType type;
    private double baseHourlyRate;
    private boolean available = true;

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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public VehicleType getType() {
        return type;
    }

    @Override
    public double getBaseHourlyRate() {
        return baseHourlyRate;
    }

    @Override
    public void setBaseHourlyRate(double newRate) {
        if (!Double.isFinite(newRate) || newRate <= 0) {
            throw new IllegalArgumentException("Hourly rate must be a finite positive number.");
        }
        this.baseHourlyRate = newRate;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public abstract String getSpecs();
}
