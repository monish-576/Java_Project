package com.ambulance.algorithms;

import com.ambulance.model.GraphNode;
import com.ambulance.model.RouteResponse;

import java.util.*;

public class BellmanFord {

    public RouteResponse findPath(Graph graph, String sourceId, String destinationId) {
        long startTime = System.nanoTime();

        if (!graph.hasNode(sourceId) || !graph.hasNode(destinationId)) {
            return emptyResult("bellmanford", startTime);
        }

        Map<String, List<Graph.WeightedNeighbor>> adj = graph.buildAdjacencyList();
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        List<String> visited = new ArrayList<>();

        for (GraphNode node : graph.getNodes()) {
            dist.put(node.getId(), Double.POSITIVE_INFINITY);
            prev.put(node.getId(), null);
        }
        dist.put(sourceId, 0.0);

        int V = graph.getNodes().size();
        List<Edge> edges = buildEdgeList(adj);

        for (int i = 0; i < V - 1; i++) {
            boolean updated = false;
            for (Edge edge : edges) {
                if (dist.get(edge.source) != Double.POSITIVE_INFINITY &&
                        dist.get(edge.source) + edge.weight < dist.get(edge.target)) {
                    dist.put(edge.target, dist.get(edge.source) + edge.weight);
                    prev.put(edge.target, edge.source);
                    updated = true;
                }
            }
            if (!updated) break;
        }

        for (Edge edge : edges) {
            if (dist.get(edge.source) != Double.POSITIVE_INFINITY &&
                    dist.get(edge.source) + edge.weight < dist.get(edge.target)) {
                long execTime = (System.nanoTime() - startTime) / 1_000_000;
                RouteResponse result = new RouteResponse();
                result.setPath(Collections.emptyList());
                result.setDistance(0);
                result.setVisitedNodes(Collections.emptyList());
                result.setExecutionTimeMs(execTime);
                result.setPathFound(false);
                result.setAlgorithm("bellmanford");
                return result;
            }
        }

        List<String> path = reconstructPath(prev, sourceId, destinationId);
        double totalDistance = dist.getOrDefault(destinationId, Double.POSITIVE_INFINITY);
        boolean pathFound = !path.isEmpty() && !Double.isInfinite(totalDistance);

        for (GraphNode node : graph.getNodes()) {
            if (dist.get(node.getId()) != Double.POSITIVE_INFINITY) {
                visited.add(node.getId());
            }
        }

        long execTime = (System.nanoTime() - startTime) / 1_000_000;
        RouteResponse result = new RouteResponse();
        result.setPath(pathFound ? path : Collections.emptyList());
        result.setDistance(pathFound ? totalDistance : 0);
        result.setVisitedNodes(visited);
        result.setExecutionTimeMs(execTime);
        result.setPathFound(pathFound);
        result.setAlgorithm("bellmanford");
        return result;
    }

    private List<Edge> buildEdgeList(Map<String, List<Graph.WeightedNeighbor>> adj) {
        List<Edge> edges = new ArrayList<>();
        for (Map.Entry<String, List<Graph.WeightedNeighbor>> entry : adj.entrySet()) {
            String source = entry.getKey();
            for (Graph.WeightedNeighbor neighbor : entry.getValue()) {
                edges.add(new Edge(source, neighbor.getNodeId(), neighbor.getWeight()));
            }
        }
        return edges;
    }

    private List<String> reconstructPath(Map<String, String> prev, String source, String destination) {
        List<String> path = new ArrayList<>();
        String current = destination;
        while (current != null) {
            path.add(current);
            current = prev.get(current);
        }
        Collections.reverse(path);
        if (path.isEmpty() || !path.get(0).equals(source)) {
            return Collections.emptyList();
        }
        return path;
    }

    private static class Edge {
        final String source;
        final String target;
        final double weight;

        Edge(String source, String target, double weight) {
            this.source = source;
            this.target = target;
            this.weight = weight;
        }
    }

    private RouteResponse emptyResult(String algorithm, long startTime) {
        long execTime = (System.nanoTime() - startTime) / 1_000_000;
        RouteResponse result = new RouteResponse();
        result.setPath(Collections.emptyList());
        result.setDistance(0);
        result.setVisitedNodes(Collections.emptyList());
        result.setExecutionTimeMs(execTime);
        result.setPathFound(false);
        result.setAlgorithm(algorithm);
        return result;
    }
}
