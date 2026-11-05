
# Vehicle Rental Service — Design Patterns & SOLID Implementation

Multi-module Java application demonstrating GoF Design Patterns and SOLID principles in an automated vehicle rental ecosystem.

---

## 🛠 Implemented Design Patterns

| Pattern | Category | Responsible Team Member | Key Classes / Interfaces |
| :--- | :--- | :--- | :--- |
| **Factory Method** | Creational | Amir | `VehicleFactory`, `CarFactory`, `BikeFactory`, `VanFactory` |
| **Builder** | Creational | Amir | `RentalBuilder`, `BaseRental` |
| **Strategy** | Behavioral | Tangharyk | `PricingStrategy`, `HourlyPricing`, `DailyPricing`, `WeeklyNightPricing` |
| **Decorator** | Structural | Tangharyk | `RentalDecorator`, `InsuranceDecorator`, `GpsDecorator`, `ChildSeatDecorator` |
| **Observer** | Behavioral | Madiyar | `AvailabilityPublisher`, `CustomerNotifier`, `FleetAnalyticsListener` |
| **Facade** | Structural | Madiyar | `RentalFacade` |

---

## 🚀 How to Build and Run

### Prerequisites
- JDK 17 or higher
- Apache Maven

### 1. Run All Tests
Run test lifecycle target via Maven window or command line.

### 2. Run Main Demonstration
Run `com.vehiclerental.Main` main class.

---

## 📁 Project Structure

```text
src/
├── main/java/com/vehiclerental/
│   ├── builder/      # RentalBuilder, BaseRental
│   ├── contract/     # Shared Interfaces & Types
│   ├── decorator/    # Decorators (Insurance, GPS, Child Seat)
│   ├── facade/       # RentalFacade
│   ├── factory/      # Vehicle Factories & Implementations
│   ├── observer/     # AvailabilityPublisher & Listeners
│   └── strategy/     # Pricing Strategies
└── test/java/com/vehiclerental/
    ├── builder/
    ├── decorator/
    ├── factory/
    ├── integration/  # Full E2E Integration Tests
    ├── observer/
    └── strategy/
