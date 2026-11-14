package com.ambulance.algorithms;

import com.ambulance.model.GraphNode;
import com.ambulance.model.RouteResponse;

import java.util.*;

public class AStar {

    public RouteResponse findPath(Graph graph, String sourceId, String destinationId) {
        long startTime = System.nanoTime();

        if (!graph.hasNode(sourceId) || !graph.hasNode(destinationId)) {
            return emptyResult("astar", startTime);
        }

        GraphNode destNode = graph.getNodeById(destinationId);
        if (destNode == null) {
            return emptyResult("astar", startTime);
        }

        Map<String, List<Graph.WeightedNeighbor>> adj = graph.buildAdjacencyList();

        Map<String, Double> gScore = new HashMap<>();
        Map<String, Double> fScore = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> closedSet = new HashSet<>();
        List<String> visitedOrder = new ArrayList<>();

        for (GraphNode node : graph.getNodes()) {
            gScore.put(node.getId(), Double.POSITIVE_INFINITY);
            fScore.put(node.getId(), Double.POSITIVE_INFINITY);
        }
        gScore.put(sourceId, 0.0);
        fScore.put(sourceId, heuristic(graph.getNodeById(sourceId), destNode));

        PriorityQueue<NodeFScore> openSet = new PriorityQueue<>(
                Comparator.comparingDouble(nf -> nf.fScore));
        openSet.offer(new NodeFScore(sourceId, fScore.get(sourceId)));

        while (!openSet.isEmpty()) {
            String current = openSet.poll().nodeId;

            if (closedSet.contains(current)) continue;
            closedSet.add(current);
            visitedOrder.add(current);

            if (current.equals(destinationId)) {
                List<String> path = reconstructPath(prev, sourceId, destinationId);
                double totalDistance = gScore.getOrDefault(destinationId, 0.0);
                boolean pathFound = !path.isEmpty();
                long execTime = (System.nanoTime() - startTime) / 1_000_000;

                RouteResponse result = new RouteResponse();
                result.setPath(pathFound ? path : Collections.emptyList());
                result.setDistance(pathFound ? totalDistance : 0);
                result.setVisitedNodes(visitedOrder);
                result.setExecutionTimeMs(execTime);
                result.setPathFound(pathFound);
                result.setAlgorithm("astar");
                return result;
            }

            for (Graph.WeightedNeighbor neighbor : adj.getOrDefault(current, Collections.emptyList())) {
                String neighborId = neighbor.getNodeId();
                if (closedSet.contains(neighborId)) continue;

                double tentativeG = gScore.get(current) + neighbor.getWeight();
                if (tentativeG < gScore.get(neighborId)) {
                    prev.put(neighborId, current);
                    gScore.put(neighborId, tentativeG);
                    fScore.put(neighborId, tentativeG + heuristic(graph.getNodeById(neighborId), destNode));
                    openSet.offer(new NodeFScore(neighborId, fScore.get(neighborId)));
                }
            }
        }

        long execTime = (System.nanoTime() - startTime) / 1_000_000;
        RouteResponse result = new RouteResponse();
        result.setPath(Collections.emptyList());
        result.setDistance(0);
        result.setVisitedNodes(visitedOrder);
        result.setExecutionTimeMs(execTime);
        result.setPathFound(false);
        result.setAlgorithm("astar");
        return result;
    }

    private double heuristic(GraphNode a, GraphNode b) {
        if (a == null || b == null) return 0;
        return a.euclideanDistanceTo(b);
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

    private static class NodeFScore {
        final String nodeId;
        final double fScore;

        NodeFScore(String nodeId, double fScore) {
            this.nodeId = nodeId;
            this.fScore = fScore;
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
