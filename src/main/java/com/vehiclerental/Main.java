package com.vehiclerental;

import com.vehiclerental.contract.ExtraType;
import com.vehiclerental.contract.Rental;
import com.vehiclerental.contract.RentalEventType;
import com.vehiclerental.contract.Vehicle;
import com.vehiclerental.contract.VehicleType;
import com.vehiclerental.facade.RentalFacade;
import com.vehiclerental.observer.CustomerNotifier;
import com.vehiclerental.observer.FleetAnalyticsListener;
import com.vehiclerental.strategy.DailyPricing;
import com.vehiclerental.strategy.HourlyPricing;
import com.vehiclerental.strategy.WeeklyNightPricing;

import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   ASTANA IT UNIVERSITY — VEHICLE RENTAL SERVICE (6 DESIGN PATTERNS DEMO)");
        System.out.println("================================================================================");

        RentalFacade facade = new RentalFacade();

        CustomerNotifier aigerim = new CustomerNotifier("Aigerim");
        CustomerNotifier nurlan = new CustomerNotifier("Nurlan");
        FleetAnalyticsListener analytics = new FleetAnalyticsListener();

        facade.addListener(aigerim);
        facade.addListener(nurlan);
        facade.addListener(analytics);

        System.out.println("\n--- [SCENARIO 1: OBSERVER SUBSCRIPTION & FACTORY METHOD FLEET REGISTRATION] ---");
        Vehicle car = facade.registerVehicle(VehicleType.CAR, "CAR-101", "Toyota Camry 70", 8.0);
        System.out.printf(Locale.US, "Registered via CarFactory:  %s | %s | $%.2f/h | %s%n",
                car.getId(), car.getModel(), car.getBaseHourlyRate(), car.getSpecs());

        Vehicle bike = facade.registerVehicle(VehicleType.BIKE, "BIKE-202", "Xiaomi Pro E-Bike", 3.0);
        System.out.printf(Locale.US, "Registered via BikeFactory: %s | %s | $%.2f/h | %s%n",
                bike.getId(), bike.getModel(), bike.getBaseHourlyRate(), bike.getSpecs());

        Vehicle van = facade.registerVehicle(VehicleType.VAN, "VAN-303", "Mercedes Sprinter", 14.0);
        System.out.printf(Locale.US, "Registered via VanFactory:  %s | %s | $%.2f/h | %s%n",
                van.getId(), van.getModel(), van.getBaseHourlyRate(), van.getSpecs());

        System.out.println("\n--- [SCENARIO 2: BUILDER + HOURLY STRATEGY + GPS DECORATOR] ---");
        Rental rental1 = facade.rentVehicle(
                "R-101", "Sanzhar", "BIKE-202", 5,
                new HourlyPricing(), List.of(ExtraType.GPS), null, null
        );
        System.out.println("Rental Created: " + rental1.getDescription());
        System.out.printf(Locale.US, "Total Price: $%.2f%n", rental1.calculateTotalPrice());

        System.out.println("\n--- [SCENARIO 3: BUILDER (PROMO 'AITU10') + DAILY STRATEGY + INSURANCE & CHILD SEAT DECORATORS] ---");
        Rental rental2 = facade.rentVehicle(
                "R-102", "Dana", "CAR-101", 48,
                new DailyPricing(), List.of(ExtraType.INSURANCE, ExtraType.CHILD_SEAT),
                "AITU10", "Airport Terminal 1"
        );
        System.out.println("Rental Created: " + rental2.getDescription());
        System.out.printf(Locale.US, "Total Price: $%.2f%n", rental2.calculateTotalPrice());

        System.out.println("\n--- [SCENARIO 4: BUILDER + WEEKLY/NIGHT STRATEGY + VAN RENTAL] ---");
        Rental rental3 = facade.rentVehicle(
                "R-103", "Timur", "VAN-303", 168,
                new WeeklyNightPricing(), List.of(ExtraType.INSURANCE, ExtraType.GPS),
                null, null
        );
        System.out.println("Rental Created: " + rental3.getDescription());
        System.out.printf(Locale.US, "Total Price: $%.2f%n", rental3.calculateTotalPrice());

        System.out.println("\n--- [SCENARIO 5: OBSERVER REAL-TIME NOTIFICATIONS (VEHICLE RETURN & PRICE DROP)] ---");
        System.out.println(">> Returning rental R-102 (Toyota Camry 70)...");
        facade.returnVehicle("R-102");

        System.out.println("\n>> Applying special weekend price drop on VAN-303 ($14.00/h -> $11.50/h)...");
        facade.applyPriceDrop("VAN-303", 11.5);

        System.out.println("\n--- [FLEET ANALYTICS SUMMARY] ---");
        System.out.println("VEHICLE_REGISTERED : " + analytics.getEventCount(RentalEventType.VEHICLE_REGISTERED));
        System.out.println("VEHICLE_RENTED     : " + analytics.getEventCount(RentalEventType.VEHICLE_RENTED));
        System.out.println("VEHICLE_AVAILABLE  : " + analytics.getEventCount(RentalEventType.VEHICLE_AVAILABLE));
        System.out.println("PRICE_DROPPED      : " + analytics.getEventCount(RentalEventType.PRICE_DROPPED));
        System.out.println("================================================================================");
    }
}