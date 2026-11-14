package com.ambulance.model;

import java.time.LocalDateTime;

public class GraphEdgeEntity {
    private String id;
    private String source;
    private String target;
    private double distance;
    private String traffic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GraphEdgeEntity() {}

    public GraphEdgeEntity(String id, String source, String target, double distance, String traffic) {
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
