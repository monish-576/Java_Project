package com.ambulance.dto;

import com.ambulance.model.GraphEdgeEntity;

public class GraphEdgeResponse {
    private String id;
    private String source;
    private String target;
    private double distance;
    private String traffic;

    public static GraphEdgeResponse fromEntity(GraphEdgeEntity entity) {
        GraphEdgeResponse r = new GraphEdgeResponse();
        r.id = entity.getId();
        r.source = entity.getSource();
        r.target = entity.getTarget();
        r.distance = entity.getDistance();
        r.traffic = entity.getTraffic();
        return r;
    }

    public String getId() { return id; }
    public String getSource() { return source; }
    public String getTarget() { return target; }
    public double getDistance() { return distance; }
    public String getTraffic() { return traffic; }
}