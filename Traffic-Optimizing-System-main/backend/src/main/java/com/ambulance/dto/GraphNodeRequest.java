package com.ambulance.dto;

import jakarta.validation.constraints.NotBlank;

public class GraphNodeRequest {
    @NotBlank(message = "Node ID is required")
    private String id;

    @NotBlank(message = "Node name is required")
    private String name;

    private double x;
    private double y;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
}