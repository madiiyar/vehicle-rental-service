package com.vehiclerental.factory;

import com.vehiclerental.contract.Vehicle;

public class BikeFactory extends VehicleFactory {
    @Override
    public Vehicle createVehicle(String id, String model, double baseHourlyRate) {
        return new Bike(id, model, baseHourlyRate);
    }
}
