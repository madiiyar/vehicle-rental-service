package com.vehiclerental.observer;

import com.vehiclerental.contract.RentalEventType;
import com.vehiclerental.contract.RentalListener;
import com.vehiclerental.contract.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerNotifier implements RentalListener {
    private final String customerName;
    private final List<String> receivedNotifications = new ArrayList<>();

    public CustomerNotifier(String customerName) {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be null or blank");
        }
        this.customerName = customerName;
    }

    @Override
    public String getListenerName() {
        return "CustomerNotifier[" + customerName + "]";
    }

    @Override
    public void onRentalEvent(RentalEventType eventType, Vehicle vehicle, String message) {
        if (eventType == RentalEventType.VEHICLE_AVAILABLE || eventType == RentalEventType.PRICE_DROPPED) {
            String formatted = "[SMS to " + customerName + "] Event " + eventType
                    + " for " + vehicle.getModel() + ": " + message;
            System.out.println(formatted);
            receivedNotifications.add(formatted);
        }
    }

    public String getCustomerName() {
        return customerName;
    }

    public List<String> getReceivedNotifications() {
        return Collections.unmodifiableList(receivedNotifications);
    }
}