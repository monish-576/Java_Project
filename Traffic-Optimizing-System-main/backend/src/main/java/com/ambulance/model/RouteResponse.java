package com.ambulance.model;

import java.util.List;

public class RouteResponse {
    private List<String> path;
    private double distance;
    private List<String> visitedNodes;
    private double estimatedTravelTime;
    private long executionTimeMs;
    private boolean pathFound;
    private String algorithm;

    public RouteResponse() {}

    public RouteResponse(List<String> path, double distance, List<String> visitedNodes,
                         long executionTimeMs, boolean pathFound, String algorithm) {
        this.path = path;
        this.distance = distance;
        this.visitedNodes = visitedNodes;
        this.executionTimeMs = executionTimeMs;
        this.pathFound = pathFound;
        this.algorithm = algorithm;
        this.estimatedTravelTime = calculateEstimatedTime(distance);
    }

    private double calculateEstimatedTime(double distanceKm) {
        double avgSpeedKmh = 40.0;
        return (distanceKm / avgSpeedKmh) * 60.0;
    }

    public List<String> getPath() { return path; }
    public void setPath(List<String> path) { this.path = path; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public List<String> getVisitedNodes() { return visitedNodes; }
    public void setVisitedNodes(List<String> visitedNodes) { this.visitedNodes = visitedNodes; }

    public double getEstimatedTravelTime() { return estimatedTravelTime; }
    public void setEstimatedTravelTime(double estimatedTravelTime) { this.estimatedTravelTime = estimatedTravelTime; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public boolean isPathFound() { return pathFound; }
    public void setPathFound(boolean pathFound) { this.pathFound = pathFound; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
}
