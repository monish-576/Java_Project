package com.ambulance.service;

import com.ambulance.algorithms.*;
import com.ambulance.model.GraphEdge;
import com.ambulance.model.GraphNode;
import com.ambulance.model.RouteHistory;
import com.ambulance.model.RouteResponse;
import com.ambulance.repository.RouteHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AlgorithmService {

    private static final Logger log = LoggerFactory.getLogger(AlgorithmService.class);
    private final RouteHistoryRepository historyRepository;

    public AlgorithmService(RouteHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
        if (historyRepository == null) {
            log.warn("RouteHistoryRepository is not available - route history will not be saved");
        }
    }

    @Transactional
    public RouteResponse findPath(String algorithm, List<GraphNode> nodes, List<GraphEdge> edges,
                                  String source, String destination) {
        long startTime = System.currentTimeMillis();
        Graph graph = new Graph(nodes, edges);

        log.info("========================================");
        log.info("  Algorithm Execution Started");
        log.info("========================================");
        log.info("Algorithm:      {}", algorithm);
        log.info("Source:         {} ({})", source,
                getNodeName(nodes, source));
        log.info("Destination:    {} ({})", destination,
                getNodeName(nodes, destination));
        log.info("Graph:          {} nodes, {} edges", nodes.size(), edges.size());

        if (!graph.hasNode(source)) {
            log.error("Source node '{}' not found in graph!", source);
            log.error("Available nodes: {}",
                    nodes.stream().map(n -> n.getId() + "(" + n.getName() + ")").collect(Collectors.toList()));
            return createEmptyResponse(algorithm, startTime);
        }
        if (!graph.hasNode(destination)) {
            log.error("Destination node '{}' not found in graph!", destination);
            return createEmptyResponse(algorithm, startTime);
        }

        RouteResponse result;
        try {
            log.debug("Starting algorithm execution...");
            result = switch (algorithm) {
                case "bfs" -> new BFS().findPath(graph, source, destination);
                case "dfs" -> new DFS().findPath(graph, source, destination);
                case "dijkstra" -> new Dijkstra().findPath(graph, source, destination);
                case "astar" -> new AStar().findPath(graph, source, destination);
                case "bellmanford" -> new BellmanFord().findPath(graph, source, destination);
                case "floydwarshall" -> new FloydWarshall().findPath(graph, source, destination);
                case "spfa" -> new SPFA().findPath(graph, source, destination);
                case "bidijkstra" -> new BidirectionalDijkstra().findPath(graph, source, destination);
                case "johnson" -> new Johnson().findPath(graph, source, destination);
                default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
            };
            log.debug("Algorithm execution completed");
        } catch (Exception e) {
            log.error("Algorithm '{}' threw exception: {}", algorithm, e.getMessage(), e);
            return createEmptyResponse(algorithm, startTime);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("----------------------------------------");
        log.info("  Results:");
        log.info("  Path found:     {}", result.isPathFound());
        log.info("  Distance:       {} km", result.getDistance());
        log.info("  Visited nodes:  {}", result.getVisitedNodes() != null ? result.getVisitedNodes().size() : 0);
        log.info("  Path length:    {} nodes", result.getPath() != null ? result.getPath().size() : 0);
        log.info("  Path:           {}", result.getPath() != null ? result.getPath().stream().collect(Collectors.joining(" -> ")) : "N/A");
        log.info("  Execution time: {} ms", result.getExecutionTimeMs());
        log.info("  Total time:     {} ms", elapsed);
        log.info("----------------------------------------");

        saveRouteHistory(source, destination, result, edges);

        return result;
    }

    private String getNodeName(List<GraphNode> nodes, String id) {
        return nodes.stream().filter(n -> n.getId().equals(id))
                .findFirst().map(GraphNode::getName).orElse("?");
    }

    private RouteResponse createEmptyResponse(String algorithm, long startTime) {
        RouteResponse empty = new RouteResponse();
        empty.setPath(List.of());
        empty.setDistance(0);
        empty.setVisitedNodes(List.of());
        empty.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        empty.setPathFound(false);
        empty.setAlgorithm(algorithm);
        return empty;
    }

    private void saveRouteHistory(String source, String destination, RouteResponse result, List<GraphEdge> edges) {
        try {
            String pathJson = "[" + (result.getPath() != null
                    ? result.getPath().stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","))
                    : "") + "]";

            String trafficJson = null;
            if (result.getPath() != null && !result.getPath().isEmpty() && edges != null) {
                StringBuilder sb = new StringBuilder("{");
                for (int i = 0; i < result.getPath().size() - 1; i++) {
                    String src = result.getPath().get(i);
                    String dst = result.getPath().get(i + 1);
                    String finalSrc = src;
                    String finalDst = dst;
                    edges.stream()
                            .filter(e -> (e.getSource().equals(finalSrc) && e.getTarget().equals(finalDst)) ||
                                         (e.getTarget().equals(finalSrc) && e.getSource().equals(finalDst)))
                            .findFirst()
                            .ifPresent(e -> {
                                if (sb.length() > 1) sb.append(",");
                                sb.append("\"").append(src).append("->").append(dst).append("\":\"")
                                        .append(e.getTraffic() != null ? e.getTraffic().toUpperCase() : "NORMAL").append("\"");
                            });
                }
                sb.append("}");
                if (sb.length() > 2) trafficJson = sb.toString();
            }

            RouteHistory history = new RouteHistory(
                    UUID.randomUUID().toString(),
                    source,
                    destination,
                    result.getAlgorithm() != null ? result.getAlgorithm().toUpperCase() : "UNKNOWN",
                    result.getDistance(),
                    pathJson,
                    result.getVisitedNodes() != null ? result.getVisitedNodes().size() : 0,
                    result.getExecutionTimeMs(),
                    result.getEstimatedTravelTime()
            );
            history.setTrafficConditions(trafficJson);
            historyRepository.save(history);
            log.info("Route history saved to MySQL: id={}, algo={}, source={}, dest={}, dist={}km",
                    history.getId(), history.getAlgorithm(), source, destination,
                    String.format("%.2f", result.getDistance()));
        } catch (Exception e) {
            log.warn("Failed to save route history to MySQL: {}", e.getMessage());
        }
    }
}
