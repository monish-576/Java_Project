package com.ambulance.model;

import java.time.LocalDateTime;

public class AccidentLocation {
    private String id;
    private String nodeId;
    private double latitude;
    private double longitude;
    private String severity;
    private String description;
    private LocalDateTime reportedAt;
    private LocalDateTime clearedAt;
    private boolean active;

    public AccidentLocation() {}

    public AccidentLocation(String id, String nodeId, double latitude, double longitude,
                            String severity, String description) {
        this.id = id;
        this.nodeId = nodeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.severity = severity;
        this.description = description;
        this.active = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    public LocalDateTime getClearedAt() { return clearedAt; }
    public void setClearedAt(LocalDateTime clearedAt) { this.clearedAt = clearedAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
