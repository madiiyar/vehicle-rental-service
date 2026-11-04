package com.vehiclerental.builder;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.Vehicle;
import com.vehiclerental.factory.Car;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RentalBuilderTest {
    private static final double DELTA = 0.000001;

    // A unit-test fixture, not Tangharyk's production pricing implementation.
    private static class TestHourlyPricing implements PricingStrategy {
        @Override
        public String getStrategyName() {
            return "Test Hourly Pricing";
        }

        @Override
        public double calculatePrice(Vehicle vehicle, int durationHours) {
            return vehicle.getBaseHourlyRate() * durationHours;
        }
    }

    private RentalBuilder validBuilder() {
        return new RentalBuilder()
                .withRentalId("RENT-101")
                .withCustomerName("Amir")
                .withVehicle(new Car("CAR-101", "Toyota Camry", 10.0))
                .withDurationHours(5)
                .withPricingStrategy(new TestHourlyPricing());
    }

    @Test
    void shouldBuildValidRentalWithRequiredFieldsOnly() {
        Rental rental = validBuilder().build();
        assertInstanceOf(BaseRental.class, rental);
        assertEquals("RENT-101", rental.getRentalId());
        assertEquals("Amir", rental.getCustomerName());
        assertEquals("CAR-101", rental.getVehicle().getId());
        assertEquals(5, rental.getDurationHours());
        assertEquals("Test Hourly Pricing", rental.getPricingStrategy().getStrategyName());
        assertEquals(50.0, rental.calculateTotalPrice(), DELTA);
        assertFalse(rental.getDescription().contains("Promo:"));
        assertFalse(rental.getDescription().contains("Notes:"));
    }

    @Test
    void shouldBuildRentalWithOptionalPromoCodeAndNotes() {
        Rental rental = validBuilder().withPromoCode("AITU10").withNotes("Need pickup at 10:00").build();
        String description = rental.getDescription();
        assertTrue(description.contains("RENT-101"));
        assertTrue(description.contains("Amir"));
        assertTrue(description.contains("Toyota Camry"));
        assertTrue(description.contains("5 hours"));
        assertTrue(description.contains("Test Hourly Pricing"));
        assertTrue(description.contains("Promo: AITU10"));
        assertTrue(description.contains("Notes: Need pickup at 10:00"));
    }

    @Test
    void shouldApplyAitu10PromoCodeDiscountCorrectly() {
        assertEquals(45.0, validBuilder().withPromoCode("AITU10").build().calculateTotalPrice(), DELTA);
        assertEquals(45.0, validBuilder().withPromoCode("aitu10").build().calculateTotalPrice(), DELTA);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenRequiredFieldMissingOrInvalidDuration() {
        assertThrows(IllegalStateException.class, () -> new RentalBuilder().build());
        assertThrows(IllegalStateException.class, () -> validBuilder().withRentalId(null).build());
        assertThrows(IllegalStateException.class, () -> validBuilder().withRentalId("  ").build());
        assertThrows(IllegalStateException.class, () -> validBuilder().withCustomerName(null).build());
        assertThrows(IllegalStateException.class, () -> validBuilder().withCustomerName("  ").build());
        assertThrows(IllegalStateException.class, () -> validBuilder().withVehicle(null).build());
        assertThrows(IllegalStateException.class, () -> validBuilder().withPricingStrategy(null).build());
        assertThrows(IllegalStateException.class, () -> validBuilder().withDurationHours(0).build());
        assertThrows(IllegalStateException.class, () -> validBuilder().withDurationHours(-5).build());
    }

    @Test
    void shouldNotDiscountUnknownOrBlankPromoCodes() {
        assertEquals(50.0, validBuilder().withPromoCode("OTHER").build().calculateTotalPrice(), DELTA);
        Rental blank = validBuilder().withPromoCode(" ").withNotes(" ").build();
        assertEquals(50.0, blank.calculateTotalPrice(), DELTA);
        assertFalse(blank.getDescription().contains("Promo:"));
        assertFalse(blank.getDescription().contains("Notes:"));
    }

    @Test
    void shouldKeepBuiltValuesWhenBuilderIsReused() {
        RentalBuilder builder = validBuilder().withNotes("First request");
        Rental first = builder.build();
        Rental second = builder.withRentalId("RENT-102").withCustomerName("Second customer")
                .withDurationHours(10).withNotes("Second request").build();
        assertEquals("RENT-101", first.getRentalId());
        assertEquals("Amir", first.getCustomerName());
        assertEquals(5, first.getDurationHours());
        assertTrue(first.getDescription().contains("First request"));
        assertEquals("RENT-102", second.getRentalId());
        assertEquals(10, second.getDurationHours());
    }

    @Test
    void shouldDelegateCalculationToTheSelectedStrategy() {
        PricingStrategy fixed = new PricingStrategy() {
            @Override
            public String getStrategyName() {
                return "Fixed test price";
            }

            @Override
            public double calculatePrice(Vehicle vehicle, int hours) {
                assertEquals("CAR-101", vehicle.getId());
                assertEquals(5, hours);
                return 200.0;
            }
        };
        Rental rental = validBuilder().withPricingStrategy(fixed).withPromoCode("AITU10").build();
        assertSame(fixed, rental.getPricingStrategy());
        assertEquals(180.0, rental.calculateTotalPrice(), DELTA);
    }

    @Test
    void shouldReportTheMissingFieldClearly() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> validBuilder().withVehicle(null).build());
        assertTrue(error.getMessage().contains("Vehicle"));
    }
}
