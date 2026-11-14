package com.ambulance.dto;

import jakarta.validation.constraints.NotBlank;

public class AmbulanceRequest {
    @NotBlank(message = "Hospital ID is required")
    private String hospitalId;

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    private String driverName;
    private String driverPhone;
    private String status;
    private Double currentLat;
    private Double currentLng;

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
}
