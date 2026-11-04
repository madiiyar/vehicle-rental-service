package com.vehiclerental.factory;

import com.vehiclerental.contract.VehicleType;

/**
 * Concrete Vehicle product representing an electric bike with an included helmet.
 */
public class Bike extends AbstractVehicle {
    private final boolean electric = true;
    private final boolean helmetIncluded = true;

    /**
     * Constructs Bike with the supplied values.
     * @param id unique vehicle identifier
     * @param model vehicle model
     * @param baseHourlyRate finite positive hourly price in USD
     * @throws IllegalArgumentException if vehicle identity, type or hourly rate is invalid
     */
    public Bike(String id, String model, double baseHourlyRate) {
        super(id, model, VehicleType.BIKE, baseHourlyRate);
    }

    /**
     * Returns the concrete vehicle specifications.
     * @return human-readable vehicle specifications
     */
    @Override
    public String getSpecs() {
        String bikeType = electric ? "Electric" : "Manual";
        String helmet = helmetIncluded ? "Helmet Included" : "Helmet Not Included";
        return "Bike [" + bikeType + ", " + helmet + "]";
    }
}

