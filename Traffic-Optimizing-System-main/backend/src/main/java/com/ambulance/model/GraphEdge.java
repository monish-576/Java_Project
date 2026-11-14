package com.ambulance.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class GraphEdge {
    private String id;

    @NotBlank(message = "Source node is required")
    private String source;

    @NotBlank(message = "Target node is required")
    private String target;

    @Positive(message = "Distance must be positive")
    private double distance;

    private String traffic = "normal";

    public GraphEdge() {}

    public GraphEdge(String id, String source, String target, double distance, String traffic) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.distance = distance;
        this.traffic = traffic;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getTraffic() { return traffic; }
    public void setTraffic(String traffic) { this.traffic = traffic; }

    public double getEffectiveDistance() {
        return switch (traffic) {
            case "blocked" -> Double.POSITIVE_INFINITY;
            case "heavy" -> distance * 2.5;
            case "moderate" -> distance * 1.5;
            default -> distance;
        };
    }
}
