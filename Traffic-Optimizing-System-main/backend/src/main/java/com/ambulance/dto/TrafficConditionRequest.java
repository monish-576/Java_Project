package com.ambulance.dto;

import jakarta.validation.constraints.NotBlank;

public class TrafficConditionRequest {
    @NotBlank(message = "Road ID is required")
    private String roadId;

    private String congestionLevel;
    private Double avgSpeedKmh;
    private String source;

    public String getRoadId() { return roadId; }
    public void setRoadId(String roadId) { this.roadId = roadId; }
    public String getCongestionLevel() { return congestionLevel; }
    public void setCongestionLevel(String congestionLevel) { this.congestionLevel = congestionLevel; }
    public Double getAvgSpeedKmh() { return avgSpeedKmh; }
    public void setAvgSpeedKmh(Double avgSpeedKmh) { this.avgSpeedKmh = avgSpeedKmh; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
