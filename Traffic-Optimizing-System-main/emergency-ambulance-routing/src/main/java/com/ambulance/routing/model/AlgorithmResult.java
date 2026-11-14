package com.ambulance.routing.model;

import java.util.List;

public class AlgorithmResult {

    private List<String> path;
    private double distance;
    private List<String> visitedNodes;
    private long executionTimeMs;
    private boolean pathFound;

    public AlgorithmResult() {}

    public AlgorithmResult(List<String> path, double distance,
                           List<String> visitedNodes, long executionTimeMs) {
        this.path = path;
        this.distance = distance;
        this.visitedNodes = visitedNodes;
        this.executionTimeMs = executionTimeMs;
        this.pathFound = path != null && !path.isEmpty();
    }

    public List<String> getPath() { return path; }
    public void setPath(List<String> path) { this.path = path; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public List<String> getVisitedNodes() { return visitedNodes; }
    public void setVisitedNodes(List<String> visitedNodes) { this.visitedNodes = visitedNodes; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public boolean isPathFound() { return pathFound; }
    public void setPathFound(boolean pathFound) { this.pathFound = pathFound; }

    @Override
    public String toString() {
        return String.format("AlgorithmResult{pathFound=%s, distance=%.2f, visits=%d, time=%dms}",
                pathFound, distance, visitedNodes != null ? visitedNodes.size() : 0, executionTimeMs);
    }
}
