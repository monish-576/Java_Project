package com.ambulance.algorithms;

import com.ambulance.model.GraphNode;
import com.ambulance.model.RouteResponse;

import java.util.*;

public class FloydWarshall {

    public RouteResponse findPath(Graph graph, String sourceId, String destinationId) {
        long startTime = System.nanoTime();

        if (!graph.hasNode(sourceId) || !graph.hasNode(destinationId)) {
            return emptyResult("floydwarshall", startTime);
        }

        List<GraphNode> nodes = graph.getNodes();
        int n = nodes.size();
        Map<String, Integer> nodeIndex = new HashMap<>();
        List<String> indexToNode = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodeIndex.put(nodes.get(i).getId(), i);
            indexToNode.add(nodes.get(i).getId());
        }

        double[][] dist = new double[n][n];
        int[][] next = new int[n][n];

        for (int i = 0; i < n; i++) {
            Arrays.fill(dist[i], Double.POSITIVE_INFINITY);
            Arrays.fill(next[i], -1);
            dist[i][i] = 0;
            next[i][i] = i;
        }

        Map<String, List<Graph.WeightedNeighbor>> adj = graph.buildAdjacencyList();
        for (Map.Entry<String, List<Graph.WeightedNeighbor>> entry : adj.entrySet()) {
            int u = nodeIndex.get(entry.getKey());
            for (Graph.WeightedNeighbor neighbor : entry.getValue()) {
                int v = nodeIndex.get(neighbor.getNodeId());
                double w = neighbor.getWeight();
                if (w < dist[u][v]) {
                    dist[u][v] = w;
                    next[u][v] = v;
                }
            }
        }

        Set<String> visitedSet = new LinkedHashSet<>();
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (dist[i][k] == Double.POSITIVE_INFINITY) continue;
                for (int j = 0; j < n; j++) {
                    if (dist[k][j] == Double.POSITIVE_INFINITY) continue;
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

        int srcIdx = nodeIndex.get(sourceId);
        int dstIdx = nodeIndex.get(destinationId);
        double totalDistance = dist[srcIdx][dstIdx];
        boolean pathFound = !Double.isInfinite(totalDistance);

        List<String> path = pathFound ? buildPath(next, srcIdx, dstIdx, indexToNode) : Collections.emptyList();

        for (int i = 0; i < n; i++) {
            if (dist[srcIdx][i] != Double.POSITIVE_INFINITY) {
                visitedSet.add(indexToNode.get(i));
            }
        }

        long execTime = (System.nanoTime() - startTime) / 1_000_000;
        RouteResponse result = new RouteResponse();
        result.setPath(pathFound ? path : Collections.emptyList());
        result.setDistance(pathFound ? totalDistance : 0);
        result.setVisitedNodes(new ArrayList<>(visitedSet));
        result.setExecutionTimeMs(execTime);
        result.setPathFound(pathFound);
        result.setAlgorithm("floydwarshall");
        return result;
    }

    private List<String> buildPath(int[][] next, int src, int dst, List<String> indexToNode) {
        if (next[src][dst] == -1) return Collections.emptyList();
        List<String> path = new ArrayList<>();
        path.add(indexToNode.get(src));
        while (src != dst) {
            src = next[src][dst];
            path.add(indexToNode.get(src));
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
