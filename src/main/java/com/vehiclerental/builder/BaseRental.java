package com.vehiclerental.builder;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.Vehicle;
import java.util.Locale;

/**
 * RentalBuilder product and concrete component for rental decorators. Price calculation delegates to the selected strategy.
 */
public class BaseRental implements Rental {
    private final String rentalId;
    private final String customerName;
    private final Vehicle vehicle;
    private final int durationHours;
    private final PricingStrategy pricingStrategy;
    private final String promoCode;
    private final String notes;

    /**
     * Stores values validated by RentalBuilder. Package access keeps construction within the builder package.
     * @param rentalId rental identifier
     * @param customerName customer name
     * @param vehicle vehicle to rent
     * @param durationHours positive duration in hours
     * @param pricingStrategy selected pricing calculation policy
     * @param promoCode optional promotional code; AITU10 applies a 10 percent discount
     * @param notes optional customer notes
     */
    BaseRental(String rentalId, String customerName, Vehicle vehicle,
               int durationHours, PricingStrategy pricingStrategy,
               String promoCode, String notes) {
        this.rentalId = rentalId;
        this.customerName = customerName;
        this.vehicle = vehicle;
        this.durationHours = durationHours;
        this.pricingStrategy = pricingStrategy;
        this.promoCode = promoCode;
        this.notes = notes;
    }

    /**
     * Returns the rental identifier.
     * @return rental identifier
     */
    @Override
    public String getRentalId() {
        return rentalId;
    }

    /**
     * Returns the customer name.
     * @return customer name
     */
    @Override
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Returns the referenced vehicle, whose rate and availability remain mutable.
     * @return referenced vehicle
     */
    @Override
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Returns the planned rental duration.
     * @return duration in hours
     */
    @Override
    public int getDurationHours() {
        return durationHours;
    }

    /**
     * Returns the selected pricing policy.
     * @return referenced pricing policy
     */
    @Override
    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    /**
     * Calculates the current strategy price and applies AITU10 when selected. Changes in the referenced vehicle rate or strategy may affect later calculations.
     * @return total price in USD before external decorators
     */
    @Override
    public double calculateTotalPrice() {
        double price = pricingStrategy.calculatePrice(vehicle, durationHours);
        if ("AITU10".equalsIgnoreCase(promoCode)) {
            price = price * 0.90;
        }
        return price;
    }

    /**
     * Describes the rental and any nonblank promotional code or notes.
     * @return human-readable rental summary
     */
    @Override
    public String getDescription() {
        String description = String.format(Locale.ROOT,
                "Rental %s | Customer: %s | Vehicle: %s | Duration: %d hours | Pricing: %s",
                rentalId, customerName, vehicle.getModel(), durationHours,
                pricingStrategy.getStrategyName());
        if (promoCode != null && !promoCode.isBlank()) {
            description += " | Promo: " + promoCode;
        }
        if (notes != null && !notes.isBlank()) {
            description += " | Notes: " + notes;
        }
        return description;
    }
}
