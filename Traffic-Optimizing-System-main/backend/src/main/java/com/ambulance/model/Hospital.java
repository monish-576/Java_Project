package com.ambulance.model;

import java.time.LocalDateTime;

public class Hospital {
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String phone;
    private String emergencyLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Hospital() {}

    public Hospital(String id, String name, String address, double latitude, double longitude,
                    String phone, String emergencyLevel) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.emergencyLevel = emergencyLevel;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmergencyLevel() { return emergencyLevel; }
    public void setEmergencyLevel(String emergencyLevel) { this.emergencyLevel = emergencyLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
