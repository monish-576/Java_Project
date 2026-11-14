package com.ambulance.model;

import java.time.LocalDateTime;

public class RouteHistory {
    private String id;
    private String sourceNode;
    private String destinationNode;
    private String algorithm;
    private double distanceKm;
    private String pathJson;
    private int visitedCount;
    private long executionTimeMs;
    private String trafficConditions;
    private Double estimatedTravelTimeMin;
    private LocalDateTime createdAt;

    public RouteHistory() {}

    public RouteHistory(String id, String sourceNode, String destinationNode, String algorithm,
                        double distanceKm, String pathJson, int visitedCount,
                        long executionTimeMs, Double estimatedTravelTimeMin) {
        this.id = id;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.algorithm = algorithm;
        this.distanceKm = distanceKm;
        this.pathJson = pathJson;
        this.visitedCount = visitedCount;
        this.executionTimeMs = executionTimeMs;
        this.estimatedTravelTimeMin = estimatedTravelTimeMin;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSourceNode() { return sourceNode; }
    public void setSourceNode(String sourceNode) { this.sourceNode = sourceNode; }
    public String getDestinationNode() { return destinationNode; }
    public void setDestinationNode(String destinationNode) { this.destinationNode = destinationNode; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public String getPathJson() { return pathJson; }
    public void setPathJson(String pathJson) { this.pathJson = pathJson; }
    public int getVisitedCount() { return visitedCount; }
    public void setVisitedCount(int visitedCount) { this.visitedCount = visitedCount; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public String getTrafficConditions() { return trafficConditions; }
    public void setTrafficConditions(String trafficConditions) { this.trafficConditions = trafficConditions; }
    public Double getEstimatedTravelTimeMin() { return estimatedTravelTimeMin; }
    public void setEstimatedTravelTimeMin(Double estimatedTravelTimeMin) { this.estimatedTravelTimeMin = estimatedTravelTimeMin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
