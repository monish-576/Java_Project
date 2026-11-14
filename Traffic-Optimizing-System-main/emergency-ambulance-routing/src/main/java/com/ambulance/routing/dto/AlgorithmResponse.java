package com.ambulance.routing.dto;

import com.ambulance.routing.model.AlgorithmResult;

import java.util.List;

public class AlgorithmResponse {

    private String algorithmName;
    private List<String> path;
    private double distance;
    private List<String> visitedNodes;
    private long executionTimeMs;
    private boolean pathFound;

    public AlgorithmResponse() {}

    public AlgorithmResponse(String algorithmName, AlgorithmResult result) {
        this.algorithmName = algorithmName;
        this.path = result.getPath();
        this.distance = result.getDistance();
        this.visitedNodes = result.getVisitedNodes();
        this.executionTimeMs = result.getExecutionTimeMs();
        this.pathFound = result.isPathFound();
    }

    public String getAlgorithmName() { return algorithmName; }
    public void setAlgorithmName(String algorithmName) { this.algorithmName = algorithmName; }

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
}
