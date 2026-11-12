package com.vehiclerental.ui;

import com.vehiclerental.builder.RentalBuilder;
import com.vehiclerental.contract.*;
import com.vehiclerental.decorator.*;
import com.vehiclerental.facade.RentalFacade;
import com.vehiclerental.strategy.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Desktop adapter. Every mutation goes through the existing facade. */
public final class RentalDesktopModel implements RentalListener {
    private final RentalFacade facade = new RentalFacade();
    private final Map<String, Vehicle> fleet = new LinkedHashMap<>();
    private final Deque<String> events = new ArrayDeque<>();
    private final EnumMap<RentalEventType,Integer> counts = new EnumMap<>(RentalEventType.class);
    private Runnable changed = () -> {};
    private int sequence = 1;
    public RentalDesktopModel() { facade.addListener(this); }
    public void onChange(Runnable listener) { changed = Objects.requireNonNull(listener); }
    public String getListenerName() { return "Desktop event monitor"; }
    public void onRentalEvent(RentalEventType type, Vehicle vehicle, String message) {
        if (type == RentalEventType.VEHICLE_REGISTERED) fleet.put(vehicle.getId(), vehicle);
        counts.merge(type, 1, Integer::sum);
        events.addFirst(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                + "  " + type + "\n" + vehicle.getId() + " · " + message);
        while (events.size() > 200) events.removeLast();
        changed.run();
    }
    public List<Vehicle> vehicles() { return List.copyOf(fleet.values()); }
    public List<Vehicle> available() { return facade.getAvailableVehicles(); }
    public List<Rental> rentals() { return List.copyOf(facade.getActiveRentals()); }
    public List<String> events() { return List.copyOf(events); }
    public int count(RentalEventType type) { return counts.getOrDefault(type, 0); }
    public void register(VehicleType type, String id, String name, double rate) {
        facade.registerVehicle(type, id, name, rate);
    }
    public void seedDemo() {
        if (!fleet.isEmpty()) return;
        register(VehicleType.CAR,"CAR-101","Toyota Camry",8);
        register(VehicleType.BIKE,"BIKE-202","Xiaomi Pro E-Bike",3);
        register(VehicleType.VAN,"VAN-303","Mercedes Sprinter",14);
        register(VehicleType.CAR,"CAR-104","Kia Sportage",12);
        register(VehicleType.BIKE,"BIKE-205","Trek City Bike",4.5);
        register(VehicleType.VAN,"VAN-306","Ford Transit",16);
    }
    public String nextRentalId() {
        String id;
        do { id = String.format(Locale.ROOT,"R-%03d",sequence++); }
        while (hasRental(id));
        return id;
    }
    private boolean hasRental(String id) { return rentals().stream().anyMatch(r -> r.getRentalId().equals(id)); }
    public record Request(String id, String customer, Vehicle vehicle, int hours,
                          String tariff, List<ExtraType> extras, String promo, String notes) {
        public Request { extras = List.copyOf(extras); }
    }
    private PricingStrategy strategy(String tariff) {
        return switch(tariff) {
            case "Daily" -> new DailyPricing();
            case "Weekly / long-term" -> new WeeklyNightPricing();
            default -> new HourlyPricing();
        };
    }
    private void validateVehicle(Request r) {
        if (r.vehicle() == null) throw new IllegalArgumentException("Choose an available vehicle first.");
        if (!r.vehicle().isAvailable()) throw new IllegalStateException("This vehicle is no longer available.");
    }
    /** Pure preview: reuse builder/strategy/decorators without reserving a vehicle. */
    public Rental quote(Request r) {
        validateVehicle(r);
        Rental result = new RentalBuilder().withRentalId(r.id()).withCustomerName(r.customer())
                .withVehicle(r.vehicle()).withDurationHours(r.hours()).withPricingStrategy(strategy(r.tariff()))
                .withPromoCode(r.promo()).withNotes(r.notes()).build();
        for (ExtraType extra : r.extras()) result = switch(extra) {
            case INSURANCE -> new InsuranceDecorator(result);
            case GPS -> new GpsDecorator(result);
            case CHILD_SEAT -> new ChildSeatDecorator(result);
        };
        return result;
    }
    public Rental book(Request r) {
        validateVehicle(r);
        return facade.rentVehicle(r.id(),r.customer(),r.vehicle().getId(),r.hours(),strategy(r.tariff()),
                r.extras(),r.promo(),r.notes());
    }
    public void returnRental(String id) { facade.returnVehicle(id); }
    public void reduceRate(Vehicle vehicle, double rate) {
        if (vehicle == null) throw new IllegalArgumentException("Choose a vehicle first.");
        if (!Double.isFinite(rate) || rate <= 0 || rate >= vehicle.getBaseHourlyRate())
            throw new IllegalArgumentException("Enter a positive rate lower than the current hourly rate.");
        facade.applyPriceDrop(vehicle.getId(),rate);
    }
}
