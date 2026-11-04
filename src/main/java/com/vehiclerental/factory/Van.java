package com.vehiclerental.factory;

import com.vehiclerental.contract.VehicleType;

/**
 * Concrete Vehicle product representing a van with 12.5 cubic metres of cargo space.
 */
public class Van extends AbstractVehicle {
    private final double cargoCapacityCubicMeters = 12.5;

    /**
     * Constructs Van with the supplied values.
     * @param id unique vehicle identifier
     * @param model vehicle model
     * @param baseHourlyRate finite positive hourly price in USD
     * @throws IllegalArgumentException if vehicle identity, type or hourly rate is invalid
     */
    public Van(String id, String model, double baseHourlyRate) {
        super(id, model, VehicleType.VAN, baseHourlyRate);
    }

    /**
     * Returns the concrete vehicle specifications.
     * @return human-readable vehicle specifications
     */
    @Override
    public String getSpecs() {
        return "Van [" + cargoCapacityCubicMeters + " m3 cargo]";
    }
}
