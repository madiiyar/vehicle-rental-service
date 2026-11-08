# Step 7C — Team Defense Walkthrough & Live-Coding Guide
**Course:** Software Design Patterns (ShP-2216) — Astana IT University  
**Defense Lead:** Madiyar  
**Team Members:** Amir, Tangharyk, Madiyar  

---

## 1. 60-Minute Cross-Teaching Protocol (15 Min Per Section)

### Part 1: Amir's Walkthrough (15 min) — `factory` & `builder`
* **Factory Method (`com.vehiclerental.factory`):**
  * **Roles:** `Vehicle` (Product Interface), `AbstractVehicle` (Shared Base), `Car` / `Bike` / `Van` (Concrete Products), `VehicleFactory` (Abstract Creator with factory method `createVehicle(...)` and template operation `orderNewVehicle(...)`), `CarFactory` / `BikeFactory` / `VanFactory` (Concrete Creators).
  * **Key Defense Point:** Eliminates `switch` creation logic in client code and satisfies the Open/Closed Principle (OCP) when adding a new vehicle type like `Scooter`.
* **Builder (`com.vehiclerental.builder`):**
  * **Roles:** `RentalBuilder` (Builder with fluent `.withX(...)` setters and `.build()`), `BaseRental` (Product with package-private constructor).
  * **Key Defense Point:** Solves the telescoping constructor anti-pattern for 5 required + 2 optional parameters (`promoCode`, `notes`) and centralizes all validation inside `build()`. `BaseRental` is package-private so external callers are forced to use `RentalBuilder`.

### Part 2: Tangharyk's Walkthrough (15 min) — `strategy` & `decorator`
* **Strategy (`com.vehiclerental.strategy`):**
  * **Roles:** `PricingStrategy` (Strategy Interface), `HourlyPricing` ($\text{base} \times \text{hours}$), `DailyPricing` ($15\%$ off for $24\text{h}+$), `WeeklyNightPricing` ($30\%$ off for $168\text{h}+$, $10\%$ off for $12\text{h}+$), `BaseRental` (Context).
  * **Key Defense Point:** Replaces conditional `if/else` tariff math inside `BaseRental` with runtime-swappable composition (`pricingStrategy.calculatePrice(vehicle, durationHours)`).
* **Decorator (`com.vehiclerental.decorator`):**
  * **Roles:** `Rental` (Component Interface), `BaseRental` (Concrete Component), `RentalDecorator` (Abstract Decorator that *implements* `Rental` and *wraps* `protected final Rental wrappedRental`), `InsuranceDecorator` ($+\$15/\text{day}$), `GpsDecorator` ($+\$10$ flat), `ChildSeatDecorator` ($+\$12$ flat).
  * **Key Defense Point:** Avoids combinatorial subclass explosion ($2^3 = 8$ subclasses for 3 extras) by stacking wrappers dynamically at runtime.

### Part 3: Madiyar's Walkthrough (15 min) — `observer`, `facade` & `Main`
* **Observer (`com.vehiclerental.observer`):**
  * **Roles:** `AvailabilityPublisher` (Subject/Publisher), `RentalListener` (Observer Interface), `CustomerNotifier` & `FleetAnalyticsListener` (Concrete Observers), `RentalEventType` (Strongly-typed event enum).
  * **Key Defense Point:** Decouples fleet state changes (`VEHICLE_REGISTERED`, `VEHICLE_RENTED`, `VEHICLE_AVAILABLE`, `PRICE_DROPPED`) from SMS alerts and analytics counters. Uses a defensive snapshot (`new ArrayList<>(listeners)`) during notification.
* **Facade (`com.vehiclerental.facade`):**
  * **Roles:** `RentalFacade` (Facade), `Main` (Client).
  * **Key Defense Point:** Provides a single simplified entry point (`registerVehicle`, `rentVehicle`, `returnVehicle`, `applyPriceDrop`) coordinating Factories, Builder, Strategies, Decorators, and Observers without locking away the underlying subsystems from unit tests.

---

## 2. 4 Live-Coding Drills (Under 3 Minutes Each — Practice in IntelliJ!)

### Drill 1 (Observer — Madiyar): Add `SecurityAuditLogger`
* **Zero existing files modified!** Create `src/main/java/com/vehiclerental/observer/SecurityAuditLogger.java`:

```java
package com.vehiclerental.observer;

import com.vehiclerental.contract.RentalEventType;
import com.vehiclerental.contract.RentalListener;
import com.vehiclerental.contract.Vehicle;

public class SecurityAuditLogger implements RentalListener {
    @Override
    public String getListenerName() {
        return "SecurityAuditLogger";
    }

    @Override
    public void onRentalEvent(RentalEventType eventType, Vehicle vehicle, String message) {
        System.out.println("[SECURITY AUDIT] " + eventType + " | Vehicle=" + vehicle.getId() + " | " + message);
    }
}
```

* Register in `Main.java`: `facade.addListener(new SecurityAuditLogger());`

### Drill 2 (Strategy — Tangharyk): Add `StudentWeekendPricing` ($25\%$ off)
* **Zero existing files modified!** Create `src/main/java/com/vehiclerental/strategy/StudentWeekendPricing.java`:

```java
package com.vehiclerental.strategy;

import com.vehiclerental.contract.PricingStrategy;
import com.vehiclerental.contract.Vehicle;

public class StudentWeekendPricing implements PricingStrategy {
    @Override
    public String getStrategyName() {
        return "Student Weekend Pricing (25% off)";
    }

    @Override
    public double calculatePrice(Vehicle vehicle, int durationHours) {
        if (vehicle == null || durationHours <= 0) {
            throw new IllegalArgumentException("Invalid vehicle or duration");
        }
        return Math.round(vehicle.getBaseHourlyRate() * durationHours * 0.75 * 100.0) / 100.0;
    }
}
```

### Drill 3 (Decorator — Tangharyk): Add `WiFiHotspotDecorator` ($+\$8.00$ flat)
1. Add `WIFI_HOTSPOT` to `ExtraType.java`.
2. Create `src/main/java/com/vehiclerental/decorator/WiFiHotspotDecorator.java`:

```java
package com.vehiclerental.decorator;

import com.vehiclerental.contract.Rental;

public class WiFiHotspotDecorator extends RentalDecorator {
    private static final double WIFI_FEE = 8.0;

    public WiFiHotspotDecorator(Rental wrappedRental) {
        super(wrappedRental);
    }

    @Override
    public double calculateTotalPrice() {
        return Math.round((super.calculateTotalPrice() + WIFI_FEE) * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + 5G WiFi Hotspot ($8.00)";
    }
}
```

3. Add `case WIFI_HOTSPOT -> new WiFiHotspotDecorator(rental);` in `RentalFacade.java`.

### Drill 4 (Factory Method — Amir): Add `Scooter` & `ScooterFactory`
1. Add `SCOOTER` to `VehicleType.java`.
2. Create `src/main/java/com/vehiclerental/factory/Scooter.java`:

```java
package com.vehiclerental.factory;

import com.vehiclerental.contract.VehicleType;

public class Scooter extends AbstractVehicle {
    public Scooter(String id, String model, double baseHourlyRate) {
        super(id, model, VehicleType.SCOOTER, baseHourlyRate);
    }

    @Override
    public String getSpecs() {
        return "Scooter [Foldable Urban E-Scooter, 30km range]";
    }
}
```

3. Create `src/main/java/com/vehiclerental/factory/ScooterFactory.java`:

```java
package com.vehiclerental.factory;

import com.vehiclerental.contract.Vehicle;

public class ScooterFactory extends VehicleFactory {
    @Override
    public Vehicle createVehicle(String id, String model, double baseHourlyRate) {
        return new Scooter(id, model, baseHourlyRate);
    }
}
```

4. Add `case SCOOTER -> new ScooterFactory();` in `RentalFacade.registerVehicle(...)`.

---

## 3. Top 5 High-Probability Defense Questions & Answers (Madiyar)

1. **Why write our own `RentalListener` and `AvailabilityPublisher` instead of `java.util.Observer`?**
   * `java.util.Observer` and `Observable` have been deprecated since Java 9 because `Observable` was a concrete class (violating composition over inheritance) and passed untyped `Object` arguments. Our `RentalListener` is a strongly-typed interface using `RentalEventType` and `Vehicle`.
2. **Why iterate over `new ArrayList<>(listeners)` inside `AvailabilityPublisher.notifyListeners(...)`?**
   * Defensive copy: if a listener unsubscribes itself during notification, iterating over a snapshot prevents a `ConcurrentModificationException`.
3. **Does `RentalFacade` lock subsystem classes away from advanced clients?**
   * No. A Facade provides a simplified default entry point (`Main.java`), while unit tests and specialized clients can still instantiate `RentalBuilder`, `CarFactory`, or `DailyPricing` directly.
4. **How does `Observer` satisfy the Open/Closed Principle (OCP) and Dependency Inversion Principle (DIP)?**
   * `AvailabilityPublisher` depends only on the `RentalListener` interface (DIP). We can add a new `SecurityAuditLogger implements RentalListener` at any time without modifying `AvailabilityPublisher` or `RentalFacade` (OCP).
5. **How do Observer and Strategy differ in intent?**
   * **Strategy** is a *one-to-one* delegation where a context (`BaseRental`) delegates a calculation (`calculatePrice`) to one interchangeable algorithm and uses its return value. **Observer** is a *one-to-many* one-way broadcast (`void onRentalEvent`) notifying multiple independent subscribers of state changes.