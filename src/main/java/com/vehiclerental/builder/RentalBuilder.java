package com.vehiclerental.builder;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.Vehicle;

/**
 * Fluent builder that validates required rental fields and constructs BaseRental products.
 */
public class RentalBuilder {
    private String rentalId;
    private String customerName;
    private Vehicle vehicle;
    private int durationHours;
    private PricingStrategy pricingStrategy;
    private String promoCode;
    private String notes;

    /**
     * Sets rentalId for the next rental construction. Validation occurs in build.
     * @param rentalId rental identifier
     * @return this builder for fluent chaining
     */
    public RentalBuilder withRentalId(String rentalId) {
        this.rentalId = rentalId;
        return this;
    }

    /**
     * Sets customerName for the next rental construction. Validation occurs in build.
     * @param customerName customer name
     * @return this builder for fluent chaining
     */
    public RentalBuilder withCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    /**
     * Sets vehicle for the next rental construction. Validation occurs in build.
     * @param vehicle vehicle to rent
     * @return this builder for fluent chaining
     */
    public RentalBuilder withVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }

    /**
     * Sets durationHours for the next rental construction. Validation occurs in build.
     * @param durationHours positive duration in hours
     * @return this builder for fluent chaining
     */
    public RentalBuilder withDurationHours(int durationHours) {
        this.durationHours = durationHours;
        return this;
    }

    /**
     * Sets pricingStrategy for the next rental construction. Validation occurs in build.
     * @param pricingStrategy selected pricing calculation policy
     * @return this builder for fluent chaining
     */
    public RentalBuilder withPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
        return this;
    }

    /**
     * Sets promoCode for the next rental construction. Validation occurs in build.
     * @param promoCode optional promotional code; AITU10 applies a 10 percent discount
     * @return this builder for fluent chaining
     */
    public RentalBuilder withPromoCode(String promoCode) {
        this.promoCode = promoCode;
        return this;
    }

    /**
     * Sets notes for the next rental construction. Validation occurs in build.
     * @param notes optional customer notes
     * @return this builder for fluent chaining
     */
    public RentalBuilder withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    /**
     * Validates all required fields and creates a rental product. Builder reuse does not change the stored field values of previously built rentals. Vehicle and strategy references remain shared.
     * @return constructed rental
     * @throws IllegalStateException if required text is blank, an object is missing or duration is not positive
     */
    public Rental build() {
        if (rentalId == null || rentalId.isBlank()) {
            throw new IllegalStateException("Rental ID is required.");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalStateException("Customer name is required.");
        }
        if (vehicle == null) {
            throw new IllegalStateException("Vehicle is required.");
        }
        if (durationHours <= 0) {
            throw new IllegalStateException("Duration must be greater than zero.");
        }
        if (pricingStrategy == null) {
            throw new IllegalStateException("Pricing strategy is required.");
        }
        return new BaseRental(rentalId.trim(), customerName.trim(), vehicle,
                durationHours, pricingStrategy, normalizeOptional(promoCode),
                normalizeOptional(notes));
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}