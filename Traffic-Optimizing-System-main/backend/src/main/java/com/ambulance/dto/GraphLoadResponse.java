package com.ambulance.dto;

import com.ambulance.model.GraphNodeEntity;
import com.ambulance.model.GraphEdgeEntity;
import java.util.List;
import java.util.stream.Collectors;

public class GraphLoadResponse {
    private List<NodeDTO> nodes;
    private List<EdgeDTO> edges;

    public GraphLoadResponse(List<GraphNodeEntity> nodeEntities, List<GraphEdgeEntity> edgeEntities) {
        this.nodes = nodeEntities.stream().map(n -> {
            NodeDTO dto = new NodeDTO();
            dto.id = n.getId();
            dto.name = n.getName();
            dto.x = n.getX();
            dto.y = n.getY();
            return dto;
        }).collect(Collectors.toList());

        this.edges = edgeEntities.stream().map(e -> {
            EdgeDTO dto = new EdgeDTO();
            dto.id = e.getId();
            dto.source = e.getSource();
            dto.target = e.getTarget();
            dto.distance = e.getDistance();
            dto.traffic = e.getTraffic();
            return dto;
        }).collect(Collectors.toList());
    }

    public List<NodeDTO> getNodes() { return nodes; }
    public List<EdgeDTO> getEdges() { return edges; }

    public static class NodeDTO {
        private String id;
        private String name;
        private double x;
        private double y;

        public String getId() { return id; }
        public String getName() { return name; }
        public double getX() { return x; }
        public double getY() { return y; }
    }

    public static class EdgeDTO {
        private String id;
        private String source;
        private String target;
        private double distance;
        private String traffic;

        public String getId() { return id; }
        public String getSource() { return source; }
        public String getTarget() { return target; }
        public double getDistance() { return distance; }
        public String getTraffic() { return traffic; }
    }
}
