package com.ambulance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class RoadNetworkRequest {
    @NotBlank(message = "Source node is required")
    private String sourceNode;

    @NotBlank(message = "Target node is required")
    private String targetNode;

    @Positive(message = "Distance must be positive")
    private double distanceKm;

    private String roadName;
    private String roadType;
    private Integer speedLimit;
    private boolean oneWay;

    public String getSourceNode() { return sourceNode; }
    public void setSourceNode(String sourceNode) { this.sourceNode = sourceNode; }
    public String getTargetNode() { return targetNode; }
    public void setTargetNode(String targetNode) { this.targetNode = targetNode; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public String getRoadName() { return roadName; }
    public void setRoadName(String roadName) { this.roadName = roadName; }
    public String getRoadType() { return roadType; }
    public void setRoadType(String roadType) { this.roadType = roadType; }
    public Integer getSpeedLimit() { return speedLimit; }
    public void setSpeedLimit(Integer speedLimit) { this.speedLimit = speedLimit; }
    public boolean isOneWay() { return oneWay; }
    public void setOneWay(boolean oneWay) { this.oneWay = oneWay; }
}
