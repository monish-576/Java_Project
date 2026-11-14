package com.ambulance.controller;

import com.ambulance.dto.RouteRequest;
import com.ambulance.model.GraphEdge;
import com.ambulance.model.GraphNode;
import com.ambulance.model.RouteResponse;
import com.ambulance.service.AlgorithmService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private static final Logger log = LoggerFactory.getLogger(RouteController.class);
    private final AlgorithmService algorithmService;

    public RouteController(AlgorithmService algorithmService) {
        this.algorithmService = algorithmService;
    }

    @GetMapping("/bfs")
    public ResponseEntity<RouteResponse> bfs(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) List<String> nodeIds,
            @RequestParam(required = false) List<String> nodeNames,
            @RequestParam(required = false) List<Integer> nodeXs,
            @RequestParam(required = false) List<Integer> nodeYs,
            @RequestParam(required = false) List<String> edgeIds,
            @RequestParam(required = false) List<String> edgeSources,
            @RequestParam(required = false) List<String> edgeTargets,
            @RequestParam(required = false) List<Double> edgeDistances,
            @RequestParam(required = false) List<String> edgeTraffics) {

        return executeAlgorithm("bfs", source, destination,
                buildNodes(nodeIds, nodeNames, nodeXs, nodeYs),
                buildEdges(edgeIds, edgeSources, edgeTargets, edgeDistances, edgeTraffics));
    }

    @GetMapping("/dfs")
    public ResponseEntity<RouteResponse> dfs(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) List<String> nodeIds,
            @RequestParam(required = false) List<String> nodeNames,
            @RequestParam(required = false) List<Integer> nodeXs,
            @RequestParam(required = false) List<Integer> nodeYs,
            @RequestParam(required = false) List<String> edgeIds,
            @RequestParam(required = false) List<String> edgeSources,
            @RequestParam(required = false) List<String> edgeTargets,
            @RequestParam(required = false) List<Double> edgeDistances,
            @RequestParam(required = false) List<String> edgeTraffics) {

        return executeAlgorithm("dfs", source, destination,
                buildNodes(nodeIds, nodeNames, nodeXs, nodeYs),
                buildEdges(edgeIds, edgeSources, edgeTargets, edgeDistances, edgeTraffics));
    }

    @GetMapping("/dijkstra")
    public ResponseEntity<RouteResponse> dijkstra(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) List<String> nodeIds,
            @RequestParam(required = false) List<String> nodeNames,
            @RequestParam(required = false) List<Integer> nodeXs,
            @RequestParam(required = false) List<Integer> nodeYs,
            @RequestParam(required = false) List<String> edgeIds,
            @RequestParam(required = false) List<String> edgeSources,
            @RequestParam(required = false) List<String> edgeTargets,
            @RequestParam(required = false) List<Double> edgeDistances,
            @RequestParam(required = false) List<String> edgeTraffics) {

        return executeAlgorithm("dijkstra", source, destination,
                buildNodes(nodeIds, nodeNames, nodeXs, nodeYs),
                buildEdges(edgeIds, edgeSources, edgeTargets, edgeDistances, edgeTraffics));
    }

    @GetMapping("/astar")
    public ResponseEntity<RouteResponse> astar(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) List<String> nodeIds,
            @RequestParam(required = false) List<String> nodeNames,
            @RequestParam(required = false) List<Integer> nodeXs,
            @RequestParam(required = false) List<Integer> nodeYs,
            @RequestParam(required = false) List<String> edgeIds,
            @RequestParam(required = false) List<String> edgeSources,
            @RequestParam(required = false) List<String> edgeTargets,
            @RequestParam(required = false) List<Double> edgeDistances,
            @RequestParam(required = false) List<String> edgeTraffics) {

        return executeAlgorithm("astar", source, destination,
                buildNodes(nodeIds, nodeNames, nodeXs, nodeYs),
                buildEdges(edgeIds, edgeSources, edgeTargets, edgeDistances, edgeTraffics));
    }

    @GetMapping("/bellmanford")
    public ResponseEntity<RouteResponse> bellmanford(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) List<String> nodeIds,
            @RequestParam(required = false) List<String> nodeNames,
            @RequestParam(required = false) List<Integer> nodeXs,
            @RequestParam(required = false) List<Integer> nodeYs,
            @RequestParam(required = false) List<String> edgeIds,
            @RequestParam(required = false) List<String> edgeSources,
            @RequestParam(required = false) List<String> edgeTargets,
            @RequestParam(required = false) List<Double> edgeDistances,
            @RequestParam(required = false) List<String> edgeTraffics) {

        return executeAlgorithm("bellmanford", source, destination,
                buildNodes(nodeIds, nodeNames, nodeXs, nodeYs),
                buildEdges(edgeIds, edgeSources, edgeTargets, edgeDistances, edgeTraffics));
    }

    @GetMapping("/floydwarshall")
    public ResponseEntity<RouteResponse> floydwarshall(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) List<String> nodeIds,
            @RequestParam(required = false) List<String> nodeNames,
            @RequestParam(required = false) List<Integer> nodeXs,
            @RequestParam(required = false) List<Integer> nodeYs,
            @RequestParam(required = false) List<String> edgeIds,
            @RequestParam(required = false) List<String> edgeSources,
            @RequestParam(required = false) List<String> edgeTargets,
            @RequestParam(required = false) List<Double> edgeDistances,
            @RequestParam(required = false) List<String> edgeTraffics) {

        return executeAlgorithm("floydwarshall", source, destination,
                buildNodes(nodeIds, nodeNames, nodeXs, nodeYs),
                buildEdges(edgeIds, edgeSources, edgeTargets, edgeDistances, edgeTraffics));
    }

    @GetMapping("/spfa")
    public ResponseEntity<RouteResponse> spfa(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) List<String> nodeIds,
            @RequestParam(required = false) List<String> nodeNames,
            @RequestParam(required = false) List<Integer> nodeXs,
            @RequestParam(required = false) List<Integer> nodeYs,
            @RequestParam(required = false) List<String> edgeIds,
            @RequestParam(required = false) List<String> edgeSources,
            @RequestParam(required = false) List<String> edgeTargets,
            @RequestParam(required = false) List<Double> edgeDistances,
            @RequestParam(required = false) List<String> edgeTraffics) {

        return executeAlgorithm("spfa", source, destination,
                buildNodes(nodeIds, nodeNames, nodeXs, nodeYs),
                buildEdges(edgeIds, edgeSources, edgeTargets, edgeDistances, edgeTraffics));
    }

    @GetMapping("/bidijkstra")
    public ResponseEntity<RouteResponse> bidijkstra(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) List<String> nodeIds,
            @RequestParam(required = false) List<String> nodeNames,
            @RequestParam(required = false) List<Integer> nodeXs,
            @RequestParam(required = false) List<Integer> nodeYs,
            @RequestParam(required = false) List<String> edgeIds,
            @RequestParam(required = false) List<String> edgeSources,
            @RequestParam(required = false) List<String> edgeTargets,
            @RequestParam(required = false) List<Double> edgeDistances,
            @RequestParam(required = false) List<String> edgeTraffics) {

        return executeAlgorithm("bidijkstra", source, destination,
                buildNodes(nodeIds, nodeNames, nodeXs, nodeYs),
                buildEdges(edgeIds, edgeSources, edgeTargets, edgeDistances, edgeTraffics));
    }

    @GetMapping("/johnson")
    public ResponseEntity<RouteResponse> johnson(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) List<String> nodeIds,
            @RequestParam(required = false) List<String> nodeNames,
            @RequestParam(required = false) List<Integer> nodeXs,
            @RequestParam(required = false) List<Integer> nodeYs,
            @RequestParam(required = false) List<String> edgeIds,
            @RequestParam(required = false) List<String> edgeSources,
            @RequestParam(required = false) List<String> edgeTargets,
            @RequestParam(required = false) List<Double> edgeDistances,
            @RequestParam(required = false) List<String> edgeTraffics) {

        return executeAlgorithm("johnson", source, destination,
                buildNodes(nodeIds, nodeNames, nodeXs, nodeYs),
                buildEdges(edgeIds, edgeSources, edgeTargets, edgeDistances, edgeTraffics));
    }

    @PostMapping("/custom")
    public ResponseEntity<RouteResponse> custom(@Valid @RequestBody RouteRequest request) {
        log.info("=== Received route request ===");
        log.info("Algorithm: {}, Source: {}, Destination: {}",
                request.getAlgorithm(), request.getSource(), request.getDestination());
        log.info("Nodes: {}, Edges: {}",
                request.getNodes() != null ? request.getNodes().size() : 0,
                request.getEdges() != null ? request.getEdges().size() : 0);

        if (request.getAlgorithm() == null || request.getAlgorithm().isBlank()) {
            log.warn("Algorithm not specified in request");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Algorithm not specified"));
        }

        if (request.getSource() == null || request.getSource().isBlank()) {
            log.warn("Source node not specified");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Source node not specified"));
        }

        if (request.getDestination() == null || request.getDestination().isBlank()) {
            log.warn("Destination node not specified");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Destination node not specified"));
        }

        if (request.getNodes() == null || request.getNodes().isEmpty()) {
            log.warn("No nodes provided in request");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("No nodes provided"));
        }

        if (request.getEdges() == null || request.getEdges().isEmpty()) {
            log.warn("No edges provided in request");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("No edges provided"));
        }

        List<GraphNode> nodes = request.getNodes().stream()
                .map(n -> new GraphNode(n.getId(), n.getName(), n.getX(), n.getY()))
                .collect(Collectors.toList());

        List<GraphEdge> edges = request.getEdges().stream()
                .map(e -> new GraphEdge(e.getId(), e.getSource(), e.getTarget(), e.getDistance(), e.getTraffic()))
                .collect(Collectors.toList());

        log.debug("Mapped {} nodes and {} edges to domain objects", nodes.size(), edges.size());

        return executeAlgorithm(request.getAlgorithm(), request.getSource(),
                request.getDestination(), nodes, edges);
    }

    private RouteResponse createErrorResponse(String error) {
        RouteResponse resp = new RouteResponse();
        resp.setPath(List.of());
        resp.setDistance(0);
        resp.setVisitedNodes(List.of());
        resp.setPathFound(false);
        resp.setAlgorithm("unknown");
        return resp;
    }

    private ResponseEntity<RouteResponse> executeAlgorithm(String algorithm, String source,
                                                            String destination,
                                                            List<GraphNode> nodes,
                                                            List<GraphEdge> edges) {
        if (source == null || destination == null || source.isBlank() || destination.isBlank()) {
            log.warn("Source or destination is blank: src='{}', dst='{}'", source, destination);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Source or destination is blank"));
        }

        log.info("Executing algorithm '{}' from '{}' to '{}' ({} nodes, {} edges)",
                algorithm, source, destination, nodes.size(), edges.size());
        long startTs = System.currentTimeMillis();
        RouteResponse result = algorithmService.findPath(algorithm, nodes, edges, source, destination);
        long elapsed = System.currentTimeMillis() - startTs;
        log.info("Algorithm '{}' completed in {}ms: pathFound={}, pathSize={}, distance={}",
                algorithm, elapsed, result.isPathFound(),
                result.getPath() != null ? result.getPath().size() : 0,
                result.getDistance());
        return ResponseEntity.ok(result);
    }

    private List<GraphNode> buildNodes(List<String> ids, List<String> names,
                                        List<Integer> xs, List<Integer> ys) {
        if (ids == null || names == null || xs == null || ys == null) return List.of();
        int size = Math.min(ids.size(), Math.min(names.size(), Math.min(xs.size(), ys.size())));
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> new GraphNode(ids.get(i), names.get(i), xs.get(i), ys.get(i)))
                .collect(Collectors.toList());
    }

    private List<GraphEdge> buildEdges(List<String> ids, List<String> sources,
                                        List<String> targets, List<Double> distances,
                                        List<String> traffics) {
        if (ids == null || sources == null || targets == null || distances == null) return List.of();
        int size = Math.min(ids.size(), Math.min(sources.size(),
                Math.min(targets.size(), distances.size())));
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> {
                    GraphEdge e = new GraphEdge();
                    e.setId(ids.get(i));
                    e.setSource(sources.get(i));
                    e.setTarget(targets.get(i));
                    e.setDistance(distances.get(i));
                    if (traffics != null && i < traffics.size()) {
                        e.setTraffic(traffics.get(i));
                    }
                    return e;
                })
                .collect(Collectors.toList());
    }
}
