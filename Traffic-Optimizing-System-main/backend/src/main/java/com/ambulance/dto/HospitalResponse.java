package com.ambulance.dto;

import com.ambulance.model.Hospital;
import java.time.LocalDateTime;

public class HospitalResponse {
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String phone;
    private String emergencyLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HospitalResponse fromEntity(Hospital h) {
        HospitalResponse r = new HospitalResponse();
        r.id = h.getId();
        r.name = h.getName();
        r.address = h.getAddress();
        r.latitude = h.getLatitude();
        r.longitude = h.getLongitude();
        r.phone = h.getPhone();
        r.emergencyLevel = h.getEmergencyLevel();
        r.createdAt = h.getCreatedAt();
        r.updatedAt = h.getUpdatedAt();
        return r;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getPhone() { return phone; }
    public String getEmergencyLevel() { return emergencyLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
