package com.vehiclerental.factory;

import com.vehiclerental.contract.VehicleType;

public class Bike extends AbstractVehicle {
    private final boolean electric = true;
    private final boolean helmetIncluded = true;

    public Bike(String id, String model, double baseHourlyRate) {
        super(id, model, VehicleType.BIKE, baseHourlyRate);
    }

    @Override
    public String getSpecs() {
        String bikeType = electric ? "Electric" : "Manual";
        String helmet = helmetIncluded ? "Helmet Included" : "Helmet Not Included";
        return "Bike [" + bikeType + ", " + helmet + "]";
    }
}
