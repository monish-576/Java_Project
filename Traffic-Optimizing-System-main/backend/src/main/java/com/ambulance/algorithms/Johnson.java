package com.ambulance.algorithms;

import com.ambulance.model.GraphNode;
import com.ambulance.model.RouteResponse;

import java.util.*;

public class Johnson {

    public RouteResponse findPath(Graph graph, String sourceId, String destinationId) {
        long startTime = System.nanoTime();

        if (!graph.hasNode(sourceId) || !graph.hasNode(destinationId)) {
            return emptyResult("johnson", startTime);
        }

        Map<String, List<Graph.WeightedNeighbor>> adj = graph.buildAdjacencyList();

        Map<String, Double> potential = computePotentials(adj, graph.getNodes());
        if (potential == null) {
            long execTime = (System.nanoTime() - startTime) / 1_000_000;
            RouteResponse result = new RouteResponse();
            result.setPath(Collections.emptyList());
            result.setDistance(0);
            result.setVisitedNodes(Collections.emptyList());
            result.setExecutionTimeMs(execTime);
            result.setPathFound(false);
            result.setAlgorithm("johnson");
            return result;
        }

        Map<String, List<Graph.WeightedNeighbor>> reweightedAdj = reweightGraph(adj, potential);

        return dijkstraOnReweighted(reweightedAdj, graph.getNodes(), sourceId, destinationId, potential, startTime);
    }

    private Map<String, Double> computePotentials(Map<String, List<Graph.WeightedNeighbor>> adj,
                                                   List<GraphNode> nodes) {
        String virtual = "__virtual__";
        Map<String, Double> dist = new HashMap<>();
        for (GraphNode node : nodes) {
            dist.put(node.getId(), Double.POSITIVE_INFINITY);
        }
        dist.put(virtual, 0.0);

        List<JohnsonEdge> edges = new ArrayList<>();
        for (Map.Entry<String, List<Graph.WeightedNeighbor>> entry : adj.entrySet()) {
            String u = entry.getKey();
            for (Graph.WeightedNeighbor nb : entry.getValue()) {
                edges.add(new JohnsonEdge(u, nb.getNodeId(), nb.getWeight()));
            }
        }
        for (GraphNode node : nodes) {
            edges.add(new JohnsonEdge(virtual, node.getId(), 0));
        }

        int V = nodes.size();
        for (int i = 0; i < V; i++) {
            boolean updated = false;
            for (JohnsonEdge e : edges) {
                if (dist.getOrDefault(e.source, Double.POSITIVE_INFINITY) != Double.POSITIVE_INFINITY &&
                        dist.getOrDefault(e.source, Double.POSITIVE_INFINITY) + e.weight < dist.getOrDefault(e.target, Double.POSITIVE_INFINITY)) {
                    dist.put(e.target, dist.getOrDefault(e.source, Double.POSITIVE_INFINITY) + e.weight);
                    updated = true;
                }
            }
            if (!updated) break;
        }

        for (JohnsonEdge e : edges) {
            if (dist.getOrDefault(e.source, Double.POSITIVE_INFINITY) != Double.POSITIVE_INFINITY &&
                    dist.getOrDefault(e.source, Double.POSITIVE_INFINITY) + e.weight < dist.getOrDefault(e.target, Double.POSITIVE_INFINITY)) {
                return null;
            }
        }

        dist.remove(virtual);
        return dist;
    }

    private Map<String, List<Graph.WeightedNeighbor>> reweightGraph(
            Map<String, List<Graph.WeightedNeighbor>> adj, Map<String, Double> potential) {

        Map<String, List<Graph.WeightedNeighbor>> reweighted = new HashMap<>();
        for (Map.Entry<String, List<Graph.WeightedNeighbor>> entry : adj.entrySet()) {
            String u = entry.getKey();
            List<Graph.WeightedNeighbor> newNeighbors = new ArrayList<>();
            for (Graph.WeightedNeighbor nb : entry.getValue()) {
                double newWeight = nb.getWeight() + potential.get(u) - potential.get(nb.getNodeId());
                newNeighbors.add(new Graph.WeightedNeighbor(nb.getNodeId(), newWeight));
            }
            reweighted.put(u, newNeighbors);
        }
        return reweighted;
    }

    private RouteResponse dijkstraOnReweighted(Map<String, List<Graph.WeightedNeighbor>> adj,
                                                List<GraphNode> nodes, String sourceId, String destinationId,
                                                Map<String, Double> potential, long startTime) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new LinkedHashSet<>();

        for (GraphNode node : nodes) {
            dist.put(node.getId(), Double.POSITIVE_INFINITY);
            prev.put(node.getId(), null);
        }
        dist.put(sourceId, 0.0);

        PriorityQueue<NodeDist> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.dist));
        pq.offer(new NodeDist(sourceId, 0.0));

        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            String currentId = current.nodeId;

            if (visited.contains(currentId)) continue;
            visited.add(currentId);

            if (currentId.equals(destinationId)) break;

            for (Graph.WeightedNeighbor neighbor : adj.getOrDefault(currentId, Collections.emptyList())) {
                String neighborId = neighbor.getNodeId();
                if (visited.contains(neighborId)) continue;

                double alt = dist.get(currentId) + neighbor.getWeight();
                if (alt < dist.get(neighborId)) {
                    dist.put(neighborId, alt);
                    prev.put(neighborId, currentId);
                    pq.offer(new NodeDist(neighborId, alt));
                }
            }
        }

        List<String> path = reconstructPath(prev, sourceId, destinationId);
        double reweightedDist = dist.getOrDefault(destinationId, Double.POSITIVE_INFINITY);
        boolean pathFound = !path.isEmpty() && !Double.isInfinite(reweightedDist);

        double actualDistance = pathFound
                ? reweightedDist - potential.get(sourceId) + potential.get(destinationId)
                : 0;

        long execTime = (System.nanoTime() - startTime) / 1_000_000;
        RouteResponse result = new RouteResponse();
        result.setPath(pathFound ? path : Collections.emptyList());
        result.setDistance(actualDistance);
        result.setVisitedNodes(new ArrayList<>(visited));
        result.setExecutionTimeMs(execTime);
        result.setPathFound(pathFound);
        result.setAlgorithm("johnson");
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

    private static class NodeDist {
        final String nodeId;
        final double dist;

        NodeDist(String nodeId, double dist) {
            this.nodeId = nodeId;
            this.dist = dist;
        }
    }

    private static class JohnsonEdge {
        final String source;
        final String target;
        final double weight;

        JohnsonEdge(String source, String target, double weight) {
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
