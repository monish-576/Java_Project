package com.ambulance.model;

import java.time.LocalDateTime;

public class Ambulance {
    private String id;
    private String hospitalId;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    private String status;
    private Double currentLat;
    private Double currentLng;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

    public Ambulance() {}

    public Ambulance(String id, String hospitalId, String vehicleNumber, String driverName,
                     String driverPhone, String status) {
        this.id = id;
        this.hospitalId = hospitalId;
        this.vehicleNumber = vehicleNumber;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getCurrentLat() { return currentLat; }
    public void setCurrentLat(Double currentLat) { this.currentLat = currentLat; }
    public Double getCurrentLng() { return currentLng; }
    public void setCurrentLng(Double currentLng) { this.currentLng = currentLng; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
