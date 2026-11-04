package com.vehiclerental.factory;

import com.vehiclerental.contract.Vehicle;

/**
 * Factory Method creator. Subclasses choose the concrete product through createVehicle.
 */
public abstract class VehicleFactory {
    // Factory Method: each subclass chooses the concrete vehicle.
    /**
     * Factory Method that creates the corresponding concrete Vehicle product.
     * @param id unique vehicle identifier
     * @param model vehicle model
     * @param baseHourlyRate finite positive hourly price in USD
     * @return new vehicle product
     */
    public abstract Vehicle createVehicle(String id, String model, double baseHourlyRate);

    // Common operation shared by every concrete factory.
    /**
     * Creates a vehicle through the factory method and initializes its availability.
     * @param id unique vehicle identifier
     * @param model vehicle model
     * @param baseHourlyRate finite positive hourly price in USD
     * @return new available vehicle
     */
    public Vehicle orderNewVehicle(String id, String model, double baseHourlyRate) {
        Vehicle vehicle = createVehicle(id, model, baseHourlyRate);
        vehicle.setAvailable(true);
        return vehicle;
    }
}
