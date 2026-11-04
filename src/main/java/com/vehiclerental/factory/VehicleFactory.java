package com.vehiclerental.factory;

import com.vehiclerental.contract.Vehicle;

public abstract class VehicleFactory {
    // Factory Method: each subclass chooses the concrete vehicle.
    public abstract Vehicle createVehicle(String id, String model, double baseHourlyRate);

    // Common operation shared by every concrete factory.
    public Vehicle orderNewVehicle(String id, String model, double baseHourlyRate) {
        Vehicle vehicle = createVehicle(id, model, baseHourlyRate);
        vehicle.setAvailable(true);
        return vehicle;
    }
}
