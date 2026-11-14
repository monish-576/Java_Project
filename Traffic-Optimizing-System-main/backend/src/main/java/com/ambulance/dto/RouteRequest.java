package com.ambulance.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class RouteRequest {
    @NotBlank(message = "Source node ID is required")
    private String source;

    @NotBlank(message = "Destination node ID is required")
    private String destination;

    private String algorithm;

    private List<NodeDTO> nodes;
    private List<EdgeDTO> edges;

    public static class NodeDTO {
        private String id;
        private String name;
        private int x;
        private int y;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
    }

    public static class EdgeDTO {
        private String id;
        private String source;
        private String target;
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

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public List<NodeDTO> getNodes() { return nodes; }
    public void setNodes(List<NodeDTO> nodes) { this.nodes = nodes; }

    public List<EdgeDTO> getEdges() { return edges; }
    public void setEdges(List<EdgeDTO> edges) { this.edges = edges; }
}
