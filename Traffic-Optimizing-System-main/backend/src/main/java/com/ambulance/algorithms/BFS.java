package com.ambulance.algorithms;

import com.ambulance.model.RouteResponse;

import java.util.*;

public class BFS {

    public RouteResponse findPath(Graph graph, String sourceId, String destinationId) {
        long startTime = System.nanoTime();

        if (!graph.hasNode(sourceId) || !graph.hasNode(destinationId)) {
            return emptyResult("bfs", startTime);
        }

        Map<String, List<Graph.WeightedNeighbor>> adj = graph.buildAdjacencyList();

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

            for (Graph.WeightedNeighbor neighbor : adj.getOrDefault(current, Collections.emptyList())) {
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

        long execTime = (System.nanoTime() - startTime) / 1_000_000;
        RouteResponse result = new RouteResponse();
        result.setPath(pathFound ? path : Collections.emptyList());
        result.setDistance(pathFound ? totalDistance : 0);
        result.setVisitedNodes(visitedOrder);
        result.setExecutionTimeMs(execTime);
        result.setPathFound(pathFound);
        result.setAlgorithm("bfs");
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
