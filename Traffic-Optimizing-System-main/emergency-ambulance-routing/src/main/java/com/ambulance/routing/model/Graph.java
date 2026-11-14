package com.ambulance.routing.model;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    private String id;
    private String name;
    private final List<Node> nodes;
    private final List<Edge> edges;

    public Graph() {
        this.id = UUID.randomUUID().toString();
        this.name = "Untitled Graph";
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public Graph(String id, String name) {
        this.id = id;
        this.name = name;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Node> getNodes() { return Collections.unmodifiableList(nodes); }
    public List<Edge> getEdges() { return Collections.unmodifiableList(edges); }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void removeNode(String nodeId) {
        nodes.removeIf(n -> n.getId().equals(nodeId));
        edges.removeIf(e -> e.getSourceId().equals(nodeId) || e.getTargetId().equals(nodeId));
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public void removeEdge(String edgeId) {
        edges.removeIf(e -> e.getId().equals(edgeId));
    }

    public Node getNodeById(String id) {
        return nodes.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Node> getNodesByType(Node.NodeType type) {
        return nodes.stream()
                .filter(n -> n.getNodeType() == type)
                .collect(Collectors.toList());
    }

    public Map<String, List<Edge.WeightedNeighbor>> buildAdjacencyList() {
        Map<String, List<Edge.WeightedNeighbor>> adj = new HashMap<>();
        for (Node node : nodes) {
            adj.put(node.getId(), new ArrayList<>());
        }
        for (Edge edge : edges) {
            double effectiveDist = edge.getEffectiveDistance();
            if (!Double.isInfinite(effectiveDist)) {
                adj.get(edge.getSourceId()).add(
                        new Edge.WeightedNeighbor(edge.getTargetId(), effectiveDist));
                adj.get(edge.getTargetId()).add(
                        new Edge.WeightedNeighbor(edge.getSourceId(), effectiveDist));
            }
        }
        return adj;
    }

    public double[][] buildAdjacencyMatrix() {
        int size = nodes.size();
        double[][] matrix = new double[size][size];
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            indexMap.put(nodes.get(i).getId(), i);
        }
        for (int i = 0; i < size; i++) {
            Arrays.fill(matrix[i], Double.POSITIVE_INFINITY);
            matrix[i][i] = 0;
        }
        for (Edge edge : edges) {
            Integer si = indexMap.get(edge.getSourceId());
            Integer ti = indexMap.get(edge.getTargetId());
            if (si != null && ti != null) {
                double eff = edge.getEffectiveDistance();
                matrix[si][ti] = eff;
                matrix[ti][si] = eff;
            }
        }
        return matrix;
    }

    public int getNodeCount() { return nodes.size(); }

    public int getEdgeCount() { return edges.size(); }

    public boolean hasNode(String nodeId) {
        return nodes.stream().anyMatch(n -> n.getId().equals(nodeId));
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }

    @Override
    public String toString() {
        return String.format("Graph{id='%s', name='%s', nodes=%d, edges=%d}", id, name, nodes.size(), edges.size());
    }
}
