package com.ambulance.algorithms;

import com.ambulance.model.GraphEdge;
import com.ambulance.model.GraphNode;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {
    private final List<GraphNode> nodes;
    private final List<GraphEdge> edges;

    public Graph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public Graph(List<GraphNode> nodes, List<GraphEdge> edges) {
        this.nodes = new ArrayList<>(nodes);
        this.edges = new ArrayList<>(edges);
    }

    public List<GraphNode> getNodes() { return Collections.unmodifiableList(nodes); }
    public List<GraphEdge> getEdges() { return Collections.unmodifiableList(edges); }

    public GraphNode getNodeById(String id) {
        return nodes.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean hasNode(String nodeId) {
        return nodes.stream().anyMatch(n -> n.getId().equals(nodeId));
    }

    public Map<String, List<WeightedNeighbor>> buildAdjacencyList() {
        Map<String, List<WeightedNeighbor>> adj = new HashMap<>();
        for (GraphNode node : nodes) {
            adj.put(node.getId(), new ArrayList<>());
        }
        for (GraphEdge edge : edges) {
            double effectiveDist = edge.getEffectiveDistance();
            if (!Double.isInfinite(effectiveDist)) {
                adj.get(edge.getSource()).add(new WeightedNeighbor(edge.getTarget(), effectiveDist));
                adj.get(edge.getTarget()).add(new WeightedNeighbor(edge.getSource(), effectiveDist));
            }
        }
        return adj;
    }

    public static class WeightedNeighbor {
        private final String nodeId;
        private final double weight;

        public WeightedNeighbor(String nodeId, double weight) {
            this.nodeId = nodeId;
            this.weight = weight;
        }

        public String getNodeId() { return nodeId; }
        public double getWeight() { return weight; }
    }
}
