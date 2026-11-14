package com.ambulance.routing.algorithm;

import com.ambulance.routing.model.*;

import java.util.*;

public class BFSAlgorithm implements PathAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "Breadth-First Search (BFS)";
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
        Queue<String> queue = new LinkedList<>();

        visitedSet.add(sourceId);
        visitedOrder.add(sourceId);
        dist.put(sourceId, 0.0);
        prev.put(sourceId, null);
        queue.offer(sourceId);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(destinationId)) break;

            List<Edge.WeightedNeighbor> neighbors = adj.getOrDefault(current, Collections.emptyList());
            for (Edge.WeightedNeighbor neighbor : neighbors) {
                String neighborId = neighbor.getNodeId();
                if (!visitedSet.contains(neighborId)) {
                    visitedSet.add(neighborId);
                    visitedOrder.add(neighborId);
                    prev.put(neighborId, current);
                    dist.put(neighborId, dist.getOrDefault(current, 0.0) + neighbor.getWeight());
                    queue.offer(neighborId);
                }
            }
        }

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
