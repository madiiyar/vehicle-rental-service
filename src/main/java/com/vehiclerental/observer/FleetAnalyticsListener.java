package com.vehiclerental.observer;

import com.vehiclerental.contract.RentalEventType;
import com.vehiclerental.contract.RentalListener;
import com.vehiclerental.contract.Vehicle;

import java.util.EnumMap;
import java.util.Map;

public class FleetAnalyticsListener implements RentalListener {
    private final Map<RentalEventType, Integer> eventCounts = new EnumMap<>(RentalEventType.class);

    @Override
    public String getListenerName() {
        return "FleetAnalyticsListener";
    }

    @Override
    public void onRentalEvent(RentalEventType eventType, Vehicle vehicle, String message) {
        int newCount = eventCounts.getOrDefault(eventType, 0) + 1;
        eventCounts.put(eventType, newCount);
        System.out.println("[Analytics] Recorded " + eventType + " (Total: " + newCount + ")");
    }

    public int getEventCount(RentalEventType type) {
        return eventCounts.getOrDefault(type, 0);
    }
}