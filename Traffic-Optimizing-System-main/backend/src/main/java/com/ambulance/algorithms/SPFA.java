package com.ambulance.algorithms;

import com.ambulance.model.GraphNode;
import com.ambulance.model.RouteResponse;

import java.util.*;

public class SPFA {

    public RouteResponse findPath(Graph graph, String sourceId, String destinationId) {
        long startTime = System.nanoTime();

        if (!graph.hasNode(sourceId) || !graph.hasNode(destinationId)) {
            return emptyResult("spfa", startTime);
        }

        Map<String, List<Graph.WeightedNeighbor>> adj = graph.buildAdjacencyList();
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Map<String, Integer> inQueueCount = new HashMap<>();
        Set<String> inQueue = new HashSet<>();
        List<String> visitedOrder = new ArrayList<>();

        for (GraphNode node : graph.getNodes()) {
            dist.put(node.getId(), Double.POSITIVE_INFINITY);
            prev.put(node.getId(), null);
            inQueueCount.put(node.getId(), 0);
        }
        dist.put(sourceId, 0.0);

        Queue<String> queue = new LinkedList<>();
        queue.offer(sourceId);
        inQueue.add(sourceId);
        inQueueCount.put(sourceId, 1);

        int V = graph.getNodes().size();
        boolean negativeCycle = false;

        while (!queue.isEmpty()) {
            String current = queue.poll();
            inQueue.remove(current);
            visitedOrder.add(current);

            for (Graph.WeightedNeighbor neighbor : adj.getOrDefault(current, Collections.emptyList())) {
                String neighborId = neighbor.getNodeId();
                double weight = neighbor.getWeight();

                if (dist.get(current) + weight < dist.get(neighborId)) {
                    dist.put(neighborId, dist.get(current) + weight);
                    prev.put(neighborId, current);

                    if (!inQueue.contains(neighborId)) {
                        queue.offer(neighborId);
                        inQueue.add(neighborId);
                        int count = inQueueCount.getOrDefault(neighborId, 0) + 1;
                        inQueueCount.put(neighborId, count);
                        if (count >= V) {
                            negativeCycle = true;
                            break;
                        }
                    }
                }
            }
            if (negativeCycle) break;
        }

        if (negativeCycle) {
            long execTime = (System.nanoTime() - startTime) / 1_000_000;
            RouteResponse result = new RouteResponse();
            result.setPath(Collections.emptyList());
            result.setDistance(0);
            result.setVisitedNodes(visitedOrder);
            result.setExecutionTimeMs(execTime);
            result.setPathFound(false);
            result.setAlgorithm("spfa");
            return result;
        }

        List<String> path = reconstructPath(prev, sourceId, destinationId);
        double totalDistance = dist.getOrDefault(destinationId, Double.POSITIVE_INFINITY);
        boolean pathFound = !path.isEmpty() && !Double.isInfinite(totalDistance);

        long execTime = (System.nanoTime() - startTime) / 1_000_000;
        RouteResponse result = new RouteResponse();
        result.setPath(pathFound ? path : Collections.emptyList());
        result.setDistance(pathFound ? totalDistance : 0);
        result.setVisitedNodes(visitedOrder);
        result.setExecutionTimeMs(execTime);
        result.setPathFound(pathFound);
        result.setAlgorithm("spfa");
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
