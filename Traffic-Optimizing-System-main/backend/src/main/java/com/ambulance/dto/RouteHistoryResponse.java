package com.ambulance.dto;

import com.ambulance.model.RouteHistory;
import java.time.LocalDateTime;

public class RouteHistoryResponse {
    private String id;
    private String sourceNode;
    private String destinationNode;
    private String algorithm;
    private double distanceKm;
    private String pathJson;
    private int visitedCount;
    private long executionTimeMs;
    private Double estimatedTravelTimeMin;
    private LocalDateTime createdAt;

    public static RouteHistoryResponse fromEntity(RouteHistory rh) {
        RouteHistoryResponse r = new RouteHistoryResponse();
        r.id = rh.getId();
        r.sourceNode = rh.getSourceNode();
        r.destinationNode = rh.getDestinationNode();
        r.algorithm = rh.getAlgorithm();
        r.distanceKm = rh.getDistanceKm();
        r.pathJson = rh.getPathJson();
        r.visitedCount = rh.getVisitedCount();
        r.executionTimeMs = rh.getExecutionTimeMs();
        r.estimatedTravelTimeMin = rh.getEstimatedTravelTimeMin();
        r.createdAt = rh.getCreatedAt();
        return r;
    }

    public String getId() { return id; }
    public String getSourceNode() { return sourceNode; }
    public String getDestinationNode() { return destinationNode; }
    public String getAlgorithm() { return algorithm; }
    public double getDistanceKm() { return distanceKm; }
    public String getPathJson() { return pathJson; }
    public int getVisitedCount() { return visitedCount; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public Double getEstimatedTravelTimeMin() { return estimatedTravelTimeMin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
