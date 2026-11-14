package com.ambulance.dto;

import com.ambulance.model.TrafficCondition;
import java.time.LocalDateTime;

public class TrafficConditionResponse {
    private String id;
    private String roadId;
    private String congestionLevel;
    private Double avgSpeedKmh;
    private LocalDateTime recordedAt;
    private String source;

    public static TrafficConditionResponse fromEntity(TrafficCondition tc) {
        TrafficConditionResponse r = new TrafficConditionResponse();
        r.id = tc.getId();
        r.roadId = tc.getRoadId();
        r.congestionLevel = tc.getCongestionLevel();
        r.avgSpeedKmh = tc.getAvgSpeedKmh();
        r.recordedAt = tc.getRecordedAt();
        r.source = tc.getSource();
        return r;
    }

    public String getId() { return id; }
    public String getRoadId() { return roadId; }
    public String getCongestionLevel() { return congestionLevel; }
    public Double getAvgSpeedKmh() { return avgSpeedKmh; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public String getSource() { return source; }
}
