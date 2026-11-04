package com.vehiclerental.factory;

import com.vehiclerental.contract.VehicleType;

/**
 * Concrete Vehicle product representing a five-seat automatic car.
 */
public class Car extends AbstractVehicle {
    private final int seats = 5;
    private final String transmission = "Automatic";

    /**
     * Constructs Car with the supplied values.
     * @param id unique vehicle identifier
     * @param model vehicle model
     * @param baseHourlyRate finite positive hourly price in USD
     * @throws IllegalArgumentException if vehicle identity, type or hourly rate is invalid
     */
    public Car(String id, String model, double baseHourlyRate) {
        super(id, model, VehicleType.CAR, baseHourlyRate);
    }

    /**
     * Returns the concrete vehicle specifications.
     * @return human-readable vehicle specifications
     */
    @Override
    public String getSpecs() {
        return "Car [" + seats + " seats, " + transmission + "]";
    }
}