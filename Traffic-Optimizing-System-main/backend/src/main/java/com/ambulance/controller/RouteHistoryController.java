package com.ambulance.controller;

import com.ambulance.dto.RouteHistoryResponse;
import com.ambulance.model.RouteHistory;
import com.ambulance.service.RouteHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/route-history")
public class RouteHistoryController {

    private static final Logger log = LoggerFactory.getLogger(RouteHistoryController.class);
    private final RouteHistoryService service;

    public RouteHistoryController(RouteHistoryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RouteHistoryResponse>> getAll() {
        log.info("=== Fetch all route history ===");
        List<RouteHistoryResponse> history = service.findAll();
        log.info("Returning {} route history entries", history.size());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteHistoryResponse> getById(@PathVariable String id) {
        log.info("=== Fetch route history by id ===");
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<RouteHistoryResponse> save(@RequestBody RouteHistorySaveRequest request) {
        log.info("=== Save route history ===");
        log.info("source={}, dest={}, algo={}, dist={}",
                request.getSourceNode(), request.getDestinationNode(),
                request.getAlgorithm(), request.getDistanceKm());
        RouteHistory history = new RouteHistory(
                UUID.randomUUID().toString(),
                request.getSourceNode(),
                request.getDestinationNode(),
                request.getAlgorithm(),
                request.getDistanceKm(),
                request.getPathJson(),
                request.getVisitedCount(),
                request.getExecutionTimeMs(),
                request.getEstimatedTravelTimeMin()
        );
        RouteHistory saved = service.save(history);
        log.info("Route history saved: id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(RouteHistoryResponse.fromEntity(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("=== Delete route history ===");
        service.delete(id);
        log.info("Route history deleted: {}", id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        log.info("=== Delete all route history ===");
        service.deleteAll();
        log.info("All route history deleted");
        return ResponseEntity.noContent().build();
    }

    public static class RouteHistorySaveRequest {
        private String sourceNode;
        private String destinationNode;
        private String algorithm;
        private double distanceKm;
        private String pathJson;
        private int visitedCount;
        private long executionTimeMs;
        private Double estimatedTravelTimeMin;

        public String getSourceNode() { return sourceNode; }
        public void setSourceNode(String sourceNode) { this.sourceNode = sourceNode; }
        public String getDestinationNode() { return destinationNode; }
        public void setDestinationNode(String destinationNode) { this.destinationNode = destinationNode; }
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
        public String getPathJson() { return pathJson; }
        public void setPathJson(String pathJson) { this.pathJson = pathJson; }
        public int getVisitedCount() { return visitedCount; }
        public void setVisitedCount(int visitedCount) { this.visitedCount = visitedCount; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
        public Double getEstimatedTravelTimeMin() { return estimatedTravelTimeMin; }
        public void setEstimatedTravelTimeMin(Double estimatedTravelTimeMin) { this.estimatedTravelTimeMin = estimatedTravelTimeMin; }
    }
}
