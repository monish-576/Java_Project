package com.ambulance.dto;

import com.ambulance.model.GraphNodeEntity;

public class GraphNodeResponse {
    private String id;
    private String name;
    private double x;
    private double y;

    public static GraphNodeResponse fromEntity(GraphNodeEntity entity) {
        GraphNodeResponse r = new GraphNodeResponse();
        r.id = entity.getId();
        r.name = entity.getName();
        r.x = entity.getX();
        r.y = entity.getY();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
}