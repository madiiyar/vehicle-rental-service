package com.vehiclerental.integration;

import com.vehiclerental.contract.*;
import com.vehiclerental.facade.RentalFacade;
import com.vehiclerental.strategy.DailyPricing;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FullRentalFlowIntegrationTest {

    @Test
    void shouldExecuteFullRentalFlowWithDecoratorsAndStrategy() {
        RentalFacade facade = new RentalFacade();

        // 1. Регистрация авто через Фабрику
        Vehicle car = facade.registerVehicle(VehicleType.CAR, "CAR-999", "Toyota Camry", 10.0);
        assertNotNull(car);
        assertTrue(car.isAvailable());

        // 2. Создание аренды через Builder + Strategy + Decorators
        Rental rental = facade.rentVehicle(
                "R-999",
                "Tangharyk",
                "CAR-999",
                24,
                new DailyPricing(),
                List.of(ExtraType.INSURANCE, ExtraType.GPS),
                "AITU10",
                "Integration Test"
        );

        assertNotNull(rental);
        assertFalse(car.isAvailable());
        assertTrue(rental.getDescription().contains("Full Insurance"));
        assertTrue(rental.getDescription().contains("GPS Navigation"));

        // 3. Возврат авто
        facade.returnVehicle("R-999");
        assertTrue(car.isAvailable());
    }
}