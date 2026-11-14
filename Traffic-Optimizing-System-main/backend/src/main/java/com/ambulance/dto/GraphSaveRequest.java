package com.ambulance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;

public class GraphSaveRequest {

    @Valid
    private List<NodeDTO> nodes;

    @Valid
    private List<EdgeDTO> edges;

    public List<NodeDTO> getNodes() { return nodes; }
    public void setNodes(List<NodeDTO> nodes) { this.nodes = nodes; }
    public List<EdgeDTO> getEdges() { return edges; }
    public void setEdges(List<EdgeDTO> edges) { this.edges = edges; }

    public static class NodeDTO {
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

    public static class EdgeDTO {
        @NotBlank(message = "Edge ID is required")
        private String id;

        @NotBlank(message = "Source node is required")
        private String source;

        @NotBlank(message = "Target node is required")
        private String target;

        @Positive(message = "Distance must be positive")
        private double distance;

        private String traffic;

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
    }
}
