package com.vehiclerental.contract;

public interface Vehicle {
    String getId();
    String getModel();
    VehicleType getType();
    double getBaseHourlyRate();
    void setBaseHourlyRate(double newRate);
    boolean isAvailable();
    void setAvailable(boolean available);
    String getSpecs();
}
