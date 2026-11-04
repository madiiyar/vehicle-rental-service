package com.vehiclerental.factory;

import com.vehiclerental.contract.Vehicle;

/**
 * Concrete creator whose factory method constructs Bike products.
 */
public class BikeFactory extends VehicleFactory {
    /**
     * Factory Method that creates the corresponding concrete Vehicle product.
     * @param id unique vehicle identifier
     * @param model vehicle model
     * @param baseHourlyRate finite positive hourly price in USD
     * @return new vehicle product
     */
    @Override
    public Vehicle createVehicle(String id, String model, double baseHourlyRate) {
        return new Bike(id, model, baseHourlyRate);
    }
}