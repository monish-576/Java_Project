package com.ambulance.dto;

import com.ambulance.model.Ambulance;
import java.time.LocalDateTime;

public class AmbulanceResponse {
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

    public static AmbulanceResponse fromEntity(Ambulance a) {
        AmbulanceResponse r = new AmbulanceResponse();
        r.id = a.getId();
        r.hospitalId = a.getHospitalId();
        r.vehicleNumber = a.getVehicleNumber();
        r.driverName = a.getDriverName();
        r.driverPhone = a.getDriverPhone();
        r.status = a.getStatus();
        r.currentLat = a.getCurrentLat();
        r.currentLng = a.getCurrentLng();
        r.lastUpdated = a.getLastUpdated();
        r.createdAt = a.getCreatedAt();
        return r;
    }

    public String getId() { return id; }
    public String getHospitalId() { return hospitalId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getDriverName() { return driverName; }
    public String getDriverPhone() { return driverPhone; }
    public String getStatus() { return status; }
    public Double getCurrentLat() { return currentLat; }
    public Double getCurrentLng() { return currentLng; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
