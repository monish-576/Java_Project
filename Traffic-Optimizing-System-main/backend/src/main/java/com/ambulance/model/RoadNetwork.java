package com.ambulance.model;

import java.time.LocalDateTime;

public class RoadNetwork {
    private String id;
    private String sourceNode;
    private String targetNode;
    private double distanceKm;
    private String roadName;
    private String roadType;
    private int speedLimit;
    private boolean oneWay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RoadNetwork() {}

    public RoadNetwork(String id, String sourceNode, String targetNode, double distanceKm,
                       String roadName, String roadType, int speedLimit, boolean oneWay) {
        this.id = id;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.distanceKm = distanceKm;
        this.roadName = roadName;
        this.roadType = roadType;
        this.speedLimit = speedLimit;
        this.oneWay = oneWay;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public int getSpeedLimit() { return speedLimit; }
    public void setSpeedLimit(int speedLimit) { this.speedLimit = speedLimit; }
    public boolean isOneWay() { return oneWay; }
    public void setOneWay(boolean oneWay) { this.oneWay = oneWay; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
