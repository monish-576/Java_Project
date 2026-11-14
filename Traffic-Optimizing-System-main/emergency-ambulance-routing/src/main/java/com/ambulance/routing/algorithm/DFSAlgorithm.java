package com.ambulance.routing.algorithm;

import com.ambulance.routing.model.*;

import java.util.*;

public class DFSAlgorithm implements PathAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "Depth-First Search (DFS)";
    }

    @Override
    public AlgorithmResult findPath(Graph graph, String sourceId, String destinationId) {
        long startTime = System.nanoTime();

        if (!graph.hasNode(sourceId) || !graph.hasNode(destinationId)) {
            return new AlgorithmResult(Collections.emptyList(), 0, Collections.emptyList(), 0);
        }

        Map<String, List<Edge.WeightedNeighbor>> adj = graph.buildAdjacencyList();

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

        long executionTime = (System.nanoTime() - startTime) / 1_000_000;

        AlgorithmResult result = new AlgorithmResult();
        result.setPath(pathFound ? path : Collections.emptyList());
        result.setDistance(pathFound ? totalDistance : 0);
        result.setVisitedNodes(visitedOrder);
        result.setExecutionTimeMs(executionTime);
        result.setPathFound(pathFound);
        return result;
    }

    private void dfsRecursive(String current, String destination,
                               Map<String, List<Edge.WeightedNeighbor>> adj,
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

        List<Edge.WeightedNeighbor> neighbors = adj.getOrDefault(current, Collections.emptyList());
        for (Edge.WeightedNeighbor neighbor : neighbors) {
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
}
