package com.vehiclerental.factory;

import com.vehiclerental.contract.Vehicle;

public class VanFactory extends VehicleFactory {
    @Override
    public Vehicle createVehicle(String id, String model, double baseHourlyRate) {
        return new Van(id, model, baseHourlyRate);
    }
}
