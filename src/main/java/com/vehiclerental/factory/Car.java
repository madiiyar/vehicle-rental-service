package com.vehiclerental.factory;

import com.vehiclerental.contract.VehicleType;

public class Car extends AbstractVehicle {
    private final int seats = 5;
    private final String transmission = "Automatic";

    public Car(String id, String model, double baseHourlyRate) {
        super(id, model, VehicleType.CAR, baseHourlyRate);
    }

    @Override
    public String getSpecs() {
        return "Car [" + seats + " seats, " + transmission + "]";
    }
}
