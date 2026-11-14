package com.ambulance.dto;

import com.ambulance.model.AccidentLocation;
import java.time.LocalDateTime;

public class AccidentLocationResponse {
    private String id;
    private String nodeId;
    private double latitude;
    private double longitude;
    private String severity;
    private String description;
    private LocalDateTime reportedAt;
    private LocalDateTime clearedAt;
    private boolean active;

    public static AccidentLocationResponse fromEntity(AccidentLocation loc) {
        AccidentLocationResponse r = new AccidentLocationResponse();
        r.id = loc.getId();
        r.nodeId = loc.getNodeId();
        r.latitude = loc.getLatitude();
        r.longitude = loc.getLongitude();
        r.severity = loc.getSeverity();
        r.description = loc.getDescription();
        r.reportedAt = loc.getReportedAt();
        r.clearedAt = loc.getClearedAt();
        r.active = loc.isActive();
        return r;
    }

    public String getId() { return id; }
    public String getNodeId() { return nodeId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public LocalDateTime getClearedAt() { return clearedAt; }
    public boolean isActive() { return active; }
}
