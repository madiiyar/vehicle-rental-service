package com.vehiclerental.factory;

import com.vehiclerental.contract.VehicleType;

public class Van extends AbstractVehicle {
    private final double cargoCapacityCubicMeters = 12.5;

    public Van(String id, String model, double baseHourlyRate) {
        super(id, model, VehicleType.VAN, baseHourlyRate);
    }

    @Override
    public String getSpecs() {
        return "Van [" + cargoCapacityCubicMeters + " m3 cargo]";
    }
}
