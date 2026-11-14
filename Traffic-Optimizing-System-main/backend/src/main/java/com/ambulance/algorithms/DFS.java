package com.ambulance.algorithms;

import com.ambulance.model.RouteResponse;

import java.util.*;

public class DFS {

    public RouteResponse findPath(Graph graph, String sourceId, String destinationId) {
        long startTime = System.nanoTime();

        if (!graph.hasNode(sourceId) || !graph.hasNode(destinationId)) {
            return emptyResult("dfs", startTime);
        }

        Map<String, List<Graph.WeightedNeighbor>> adj = graph.buildAdjacencyList();

        Set<String> visitedSet = new HashSet<>();
        List<String> visitedOrder = new ArrayList<>();
        Map<String, String> prev = new HashMap<>();
        Map<String, Double> dist = new HashMap<>();
        boolean[] found = {false};

        dfsRecursive(sourceId, destinationId, adj, visitedSet, visitedOrder,
                prev, dist, 0.0, null, found);

        List<String> path = reconstructPath(prev, sourceId, destinationId);
        double totalDistance = dist.getOrDefault(destinationId, 0.0);
        boolean pathFound = !path.isEmpty();

        long execTime = (System.nanoTime() - startTime) / 1_000_000;
        RouteResponse result = new RouteResponse();
        result.setPath(pathFound ? path : Collections.emptyList());
        result.setDistance(pathFound ? totalDistance : 0);
        result.setVisitedNodes(visitedOrder);
        result.setExecutionTimeMs(execTime);
        result.setPathFound(pathFound);
        result.setAlgorithm("dfs");
        return result;
    }

    private void dfsRecursive(String current, String destination,
                               Map<String, List<Graph.WeightedNeighbor>> adj,
                               Set<String> visitedSet, List<String> visitedOrder,
                               Map<String, String> prev, Map<String, Double> dist,
                               double currentDist, String parent, boolean[] found) {
        if (found[0]) return;

        visitedSet.add(current);
        visitedOrder.add(current);
        prev.put(current, parent);
        dist.put(current, currentDist);

        if (current.equals(destination)) {
            found[0] = true;
            return;
        }

        for (Graph.WeightedNeighbor neighbor : adj.getOrDefault(current, Collections.emptyList())) {
            String neighborId = neighbor.getNodeId();
            if (!visitedSet.contains(neighborId)) {
                dfsRecursive(neighborId, destination, adj, visitedSet, visitedOrder,
                        prev, dist, currentDist + neighbor.getWeight(), current, found);
            }
        }
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
