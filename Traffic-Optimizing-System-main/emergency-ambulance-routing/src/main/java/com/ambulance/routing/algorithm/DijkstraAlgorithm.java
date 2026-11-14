package com.ambulance.routing.algorithm;

import com.ambulance.routing.model.*;

import java.util.*;

public class DijkstraAlgorithm implements PathAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "Dijkstra's Algorithm";
    }

    @Override
    public AlgorithmResult findPath(Graph graph, String sourceId, String destinationId) {
        long startTime = System.nanoTime();

        if (!graph.hasNode(sourceId) || !graph.hasNode(destinationId)) {
            return new AlgorithmResult(Collections.emptyList(), 0, Collections.emptyList(), 0);
        }

        Map<String, List<Edge.WeightedNeighbor>> adj = graph.buildAdjacencyList();
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new LinkedHashSet<>();

        for (Node node : graph.getNodes()) {
            dist.put(node.getId(), Double.POSITIVE_INFINITY);
            prev.put(node.getId(), null);
        }
        dist.put(sourceId, 0.0);

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(
                Comparator.comparingDouble(nd -> nd.distance));
        pq.offer(new NodeDistance(sourceId, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String currentId = current.nodeId;

            if (visited.contains(currentId)) continue;
            visited.add(currentId);

            if (currentId.equals(destinationId)) break;

            List<Edge.WeightedNeighbor> neighbors = adj.getOrDefault(currentId, Collections.emptyList());
            for (Edge.WeightedNeighbor neighbor : neighbors) {
                String neighborId = neighbor.getNodeId();
                if (visited.contains(neighborId)) continue;

                double alt = dist.get(currentId) + neighbor.getWeight();
                if (alt < dist.get(neighborId)) {
                    dist.put(neighborId, alt);
                    prev.put(neighborId, currentId);
                    pq.offer(new NodeDistance(neighborId, alt));
                }
            }
        }

        List<String> path = reconstructPath(prev, sourceId, destinationId);
        double totalDistance = dist.getOrDefault(destinationId, Double.POSITIVE_INFINITY);
        boolean pathFound = !path.isEmpty() && !Double.isInfinite(totalDistance);

        long executionTime = (System.nanoTime() - startTime) / 1_000_000;

        AlgorithmResult result = new AlgorithmResult();
        result.setPath(pathFound ? path : Collections.emptyList());
        result.setDistance(pathFound ? totalDistance : 0);
        result.setVisitedNodes(new ArrayList<>(visited));
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

    private static class NodeDistance {
        final String nodeId;
        final double distance;

        NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }
}
