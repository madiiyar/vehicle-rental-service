package com.vehiclerental.observer;

import com.vehiclerental.contract.RentalEventType;
import com.vehiclerental.contract.RentalListener;
import com.vehiclerental.contract.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AvailabilityPublisher {
    private final List<RentalListener> listeners = new ArrayList<>();

    public void subscribe(RentalListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(RentalListener listener) {
        listeners.remove(listener);
    }

    public List<RentalListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public void notifyListeners(RentalEventType eventType, Vehicle vehicle, String message) {
        for (RentalListener listener : listeners) {
            listener.onRentalEvent(eventType, vehicle, message);
        }
    }
}