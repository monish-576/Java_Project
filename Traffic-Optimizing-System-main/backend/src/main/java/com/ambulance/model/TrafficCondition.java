package com.ambulance.model;

import java.time.LocalDateTime;

public class TrafficCondition {
    private String id;
    private String roadId;
    private String congestionLevel;
    private Double avgSpeedKmh;
    private LocalDateTime recordedAt;
    private String source;

    public TrafficCondition() {}

    public TrafficCondition(String id, String roadId, String congestionLevel,
                            Double avgSpeedKmh, String source) {
        this.id = id;
        this.roadId = roadId;
        this.congestionLevel = congestionLevel;
        this.avgSpeedKmh = avgSpeedKmh;
        this.source = source;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoadId() { return roadId; }
    public void setRoadId(String roadId) { this.roadId = roadId; }
    public String getCongestionLevel() { return congestionLevel; }
    public void setCongestionLevel(String congestionLevel) { this.congestionLevel = congestionLevel; }
    public Double getAvgSpeedKmh() { return avgSpeedKmh; }
    public void setAvgSpeedKmh(Double avgSpeedKmh) { this.avgSpeedKmh = avgSpeedKmh; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
