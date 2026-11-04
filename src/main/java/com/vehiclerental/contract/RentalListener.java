package com.vehiclerental.contract;

public interface RentalListener {
    String getListenerName();
    void onRentalEvent(RentalEventType eventType, Vehicle vehicle, String message);
}