package com.vehiclerental.factory;

import com.vehiclerental.contract.Vehicle;
import com.vehiclerental.contract.VehicleType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehicleFactoryTest {
    private static final double DELTA = 0.000001;

    @Test
    void shouldCreateCarWithExpectedSpecsAndAvailability() {
        Vehicle vehicle = new CarFactory().orderNewVehicle("CAR-101", "Toyota Camry", 8.0);
        assertInstanceOf(Car.class, vehicle);
        assertEquals("CAR-101", vehicle.getId());
        assertEquals("Toyota Camry", vehicle.getModel());
        assertEquals(VehicleType.CAR, vehicle.getType());
        assertEquals(8.0, vehicle.getBaseHourlyRate(), DELTA);
        assertEquals("Car [5 seats, Automatic]", vehicle.getSpecs());
        assertTrue(vehicle.isAvailable());
    }

    @Test
    void shouldCreateBikeAndVanThroughRespectiveFactories() {
        Vehicle bike = new BikeFactory().orderNewVehicle("BIKE-101", "City Bike", 3.0);
        Vehicle van = new VanFactory().orderNewVehicle("VAN-101", "Ford Transit", 15.0);
        assertInstanceOf(Bike.class, bike);
        assertEquals(VehicleType.BIKE, bike.getType());
        assertEquals("Bike [Electric, Helmet Included]", bike.getSpecs());
        assertTrue(bike.isAvailable());
        assertInstanceOf(Van.class, van);
        assertEquals(VehicleType.VAN, van.getType());
        assertEquals("Van [12.5 m3 cargo]", van.getSpecs());
        assertTrue(van.isAvailable());
    }

    @Test
    void shouldAllowUpdatingHourlyRateAndAvailability() {
        Vehicle vehicle = new CarFactory().createVehicle("CAR-101", "Toyota Camry", 8.0);
        vehicle.setBaseHourlyRate(10.0);
        assertEquals(10.0, vehicle.getBaseHourlyRate(), DELTA);
        vehicle.setAvailable(false);
        assertFalse(vehicle.isAvailable());
        vehicle.setAvailable(true);
        assertTrue(vehicle.isAvailable());
    }

    @Test
    void shouldThrowExceptionWhenCreatingVehicleWithInvalidRateOrBlankId() {
        VehicleFactory factory = new CarFactory();
        assertThrows(IllegalArgumentException.class, () -> factory.createVehicle("", "Camry", 8.0));
        assertThrows(IllegalArgumentException.class, () -> factory.createVehicle("   ", "Camry", 8.0));
        assertThrows(IllegalArgumentException.class, () -> factory.createVehicle(null, "Camry", 8.0));
        assertThrows(IllegalArgumentException.class, () -> factory.createVehicle("CAR-1", "Camry", 0));
        assertThrows(IllegalArgumentException.class, () -> factory.createVehicle("CAR-1", "Camry", -1));
    }

    @Test
    void shouldRejectMissingModel() {
        assertThrows(IllegalArgumentException.class, () -> new BikeFactory().createVehicle("B-1", null, 3));
        assertThrows(IllegalArgumentException.class, () -> new VanFactory().createVehicle("V-1", "  ", 15));
    }

    @Test
    void shouldRejectNonFiniteRatesWithoutChangingExistingRate() {
        VehicleFactory factory = new CarFactory();
        Vehicle vehicle = factory.createVehicle("C-1", "Camry", 8);
        for (double rate : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> factory.createVehicle("C-2", "Camry", rate));
            assertThrows(IllegalArgumentException.class, () -> vehicle.setBaseHourlyRate(rate));
            assertEquals(8.0, vehicle.getBaseHourlyRate(), DELTA);
        }
        assertThrows(IllegalArgumentException.class, () -> vehicle.setBaseHourlyRate(0));
        assertThrows(IllegalArgumentException.class, () -> vehicle.setBaseHourlyRate(-2));
        assertEquals(8.0, vehicle.getBaseHourlyRate(), DELTA);
    }

    @Test
    void shouldUseOverriddenFactoryMethodInOrderOperation() {
        Vehicle existing = new Bike("B-1", "City Bike", 3);
        existing.setAvailable(false);
        VehicleFactory factory = new VehicleFactory() {
            @Override
            public Vehicle createVehicle(String id, String model, double rate) {
                return existing;
            }
        };
        Vehicle ordered = factory.orderNewVehicle("B-1", "City Bike", 3);
        assertSame(existing, ordered);
        assertTrue(ordered.isAvailable());
    }

    @Test
    void shouldCreateIndependentVehicles() {
        VehicleFactory factory = new CarFactory();
        Vehicle first = factory.createVehicle("C-1", "Camry", 8);
        Vehicle second = factory.createVehicle("C-2", "Camry", 8);
        assertNotSame(first, second);
        first.setAvailable(false);
        assertTrue(second.isAvailable());
    }
}
