package com.ambulance.dto;

import com.ambulance.model.RoadNetwork;
import java.time.LocalDateTime;

public class RoadNetworkResponse {
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

    public static RoadNetworkResponse fromEntity(RoadNetwork r) {
        RoadNetworkResponse resp = new RoadNetworkResponse();
        resp.id = r.getId();
        resp.sourceNode = r.getSourceNode();
        resp.targetNode = r.getTargetNode();
        resp.distanceKm = r.getDistanceKm();
        resp.roadName = r.getRoadName();
        resp.roadType = r.getRoadType();
        resp.speedLimit = r.getSpeedLimit();
        resp.oneWay = r.isOneWay();
        resp.createdAt = r.getCreatedAt();
        resp.updatedAt = r.getUpdatedAt();
        return resp;
    }

    public String getId() { return id; }
    public String getSourceNode() { return sourceNode; }
    public String getTargetNode() { return targetNode; }
    public double getDistanceKm() { return distanceKm; }
    public String getRoadName() { return roadName; }
    public String getRoadType() { return roadType; }
    public int getSpeedLimit() { return speedLimit; }
    public boolean isOneWay() { return oneWay; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
