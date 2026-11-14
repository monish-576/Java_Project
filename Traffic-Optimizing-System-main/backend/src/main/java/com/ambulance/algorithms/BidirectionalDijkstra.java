package com.ambulance.algorithms;

import com.ambulance.model.GraphNode;
import com.ambulance.model.RouteResponse;

import java.util.*;

public class BidirectionalDijkstra {

    public RouteResponse findPath(Graph graph, String sourceId, String destinationId) {
        long startTime = System.nanoTime();

        if (!graph.hasNode(sourceId) || !graph.hasNode(destinationId)) {
            return emptyResult("bidijkstra", startTime);
        }

        if (sourceId.equals(destinationId)) {
            long execTime = (System.nanoTime() - startTime) / 1_000_000;
            RouteResponse result = new RouteResponse();
            result.setPath(List.of(sourceId));
            result.setDistance(0);
            result.setVisitedNodes(List.of(sourceId));
            result.setExecutionTimeMs(execTime);
            result.setPathFound(true);
            result.setAlgorithm("bidijkstra");
            return result;
        }

        Map<String, List<Graph.WeightedNeighbor>> adj = graph.buildAdjacencyList();

        Map<String, Double> distF = new HashMap<>();
        Map<String, String> prevF = new HashMap<>();
        Map<String, Double> distB = new HashMap<>();
        Map<String, String> prevB = new HashMap<>();
        Set<String> visitedF = new LinkedHashSet<>();
        Set<String> visitedB = new LinkedHashSet<>();

        for (GraphNode node : graph.getNodes()) {
            distF.put(node.getId(), Double.POSITIVE_INFINITY);
            distB.put(node.getId(), Double.POSITIVE_INFINITY);
            prevF.put(node.getId(), null);
            prevB.put(node.getId(), null);
        }
        distF.put(sourceId, 0.0);
        distB.put(destinationId, 0.0);

        PriorityQueue<NodeDist> pqF = new PriorityQueue<>(Comparator.comparingDouble(n -> n.dist));
        PriorityQueue<NodeDist> pqB = new PriorityQueue<>(Comparator.comparingDouble(n -> n.dist));
        pqF.offer(new NodeDist(sourceId, 0.0));
        pqB.offer(new NodeDist(destinationId, 0.0));

        String meetingNode = null;
        double bestDist = Double.POSITIVE_INFINITY;

        while (!pqF.isEmpty() && !pqB.isEmpty()) {
            if (meetingNode != null) {
                double topF = pqF.peek().dist;
                double topB = pqB.peek().dist;
                if (topF + topB >= bestDist) break;
            }

            NodeDist curF = pqF.poll();
            if (visitedF.contains(curF.nodeId)) continue;
            visitedF.add(curF.nodeId);

            if (visitedB.contains(curF.nodeId)) {
                double candidate = distF.get(curF.nodeId) + distB.get(curF.nodeId);
                if (candidate < bestDist) {
                    bestDist = candidate;
                    meetingNode = curF.nodeId;
                }
            }

            for (Graph.WeightedNeighbor nb : adj.getOrDefault(curF.nodeId, Collections.emptyList())) {
                if (visitedF.contains(nb.getNodeId())) continue;
                double alt = distF.get(curF.nodeId) + nb.getWeight();
                if (alt < distF.get(nb.getNodeId())) {
                    distF.put(nb.getNodeId(), alt);
                    prevF.put(nb.getNodeId(), curF.nodeId);
                    pqF.offer(new NodeDist(nb.getNodeId(), alt));
                }
            }

            NodeDist curB = pqB.poll();
            if (visitedB.contains(curB.nodeId)) continue;
            visitedB.add(curB.nodeId);

            if (visitedF.contains(curB.nodeId)) {
                double candidate = distF.get(curB.nodeId) + distB.get(curB.nodeId);
                if (candidate < bestDist) {
                    bestDist = candidate;
                    meetingNode = curB.nodeId;
                }
            }

            for (Graph.WeightedNeighbor nb : adj.getOrDefault(curB.nodeId, Collections.emptyList())) {
                if (visitedB.contains(nb.getNodeId())) continue;
                double alt = distB.get(curB.nodeId) + nb.getWeight();
                if (alt < distB.get(nb.getNodeId())) {
                    distB.put(nb.getNodeId(), alt);
                    prevB.put(nb.getNodeId(), curB.nodeId);
                    pqB.offer(new NodeDist(nb.getNodeId(), alt));
                }
            }
        }

        boolean pathFound = meetingNode != null;
        List<String> path = pathFound ? buildBidirectionalPath(prevF, prevB, sourceId, destinationId, meetingNode) : Collections.emptyList();
        double totalDistance = pathFound ? bestDist : Double.POSITIVE_INFINITY;

        List<String> allVisited = new ArrayList<>(visitedF);
        allVisited.addAll(visitedB);

        long execTime = (System.nanoTime() - startTime) / 1_000_000;
        RouteResponse result = new RouteResponse();
        result.setPath(pathFound ? path : Collections.emptyList());
        result.setDistance(pathFound ? totalDistance : 0);
        result.setVisitedNodes(allVisited);
        result.setExecutionTimeMs(execTime);
        result.setPathFound(pathFound);
        result.setAlgorithm("bidijkstra");
        return result;
    }

    private List<String> buildBidirectionalPath(Map<String, String> prevF, Map<String, String> prevB,
                                                 String source, String destination, String meeting) {
        List<String> pathF = new ArrayList<>();
        String cur = meeting;
        while (cur != null) {
            pathF.add(cur);
            cur = prevF.get(cur);
        }
        Collections.reverse(pathF);

        List<String> pathB = new ArrayList<>();
        cur = prevB.get(meeting);
        while (cur != null) {
            pathB.add(cur);
            cur = prevB.get(cur);
        }

        if (pathF.isEmpty() || !pathF.get(0).equals(source)) return Collections.emptyList();
        List<String> fullPath = new ArrayList<>(pathF);
        fullPath.addAll(pathB);
        return fullPath;
    }

    private static class NodeDist {
        final String nodeId;
        final double dist;

        NodeDist(String nodeId, double dist) {
            this.nodeId = nodeId;
            this.dist = dist;
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
