package com.vehiclerental.factory;

import com.vehiclerental.contract.Vehicle;

public class CarFactory extends VehicleFactory {
    @Override
    public Vehicle createVehicle(String id, String model, double baseHourlyRate) {
        return new Car(id, model, baseHourlyRate);
    }
}
