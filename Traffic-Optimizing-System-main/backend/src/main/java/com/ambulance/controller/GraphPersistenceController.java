package com.ambulance.controller;

import com.ambulance.dto.*;
import com.ambulance.model.GraphEdgeEntity;
import com.ambulance.model.GraphNodeEntity;
import com.ambulance.service.GraphPersistenceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
public class GraphPersistenceController {

    private static final Logger log = LoggerFactory.getLogger(GraphPersistenceController.class);
    private final GraphPersistenceService service;

    public GraphPersistenceController(GraphPersistenceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody GraphSaveRequest request) {
        log.info("=== Graph bulk save ===");
        log.info("Nodes: {}, Edges: {}",
                request.getNodes() != null ? request.getNodes().size() : 0,
                request.getEdges() != null ? request.getEdges().size() : 0);
        service.save(request);
        log.info("Graph bulk save completed");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<GraphLoadResponse> load() {
        log.info("=== Graph load ===");
        GraphLoadResponse response = service.load();
        log.info("Graph load completed: {} nodes, {} edges",
                response.getNodes() != null ? response.getNodes().size() : 0,
                response.getEdges() != null ? response.getEdges().size() : 0);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear() {
        log.info("=== Graph clear ===");
        service.clear();
        log.info("Graph cleared");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/nodes")
    public ResponseEntity<GraphNodeResponse> addNode(@Valid @RequestBody GraphNodeRequest request) {
        log.info("=== Node create ===");
        log.info("id={}, name={}, x={}, y={}", request.getId(), request.getName(), request.getX(), request.getY());
        GraphNodeEntity node = new GraphNodeEntity(request.getId(), request.getName(), request.getX(), request.getY());
        GraphNodeEntity saved = service.addNode(node);
        log.info("Node created: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(GraphNodeResponse.fromEntity(saved));
    }

    @PutMapping("/nodes/{id}")
    public ResponseEntity<GraphNodeResponse> updateNode(@PathVariable String id,
                                                          @Valid @RequestBody GraphNodeUpdateRequest request) {
        log.info("=== Node update ===");
        log.info("id={}, name={}, x={}, y={}", id, request.getName(), request.getX(), request.getY());
        GraphNodeEntity updated = service.updateNode(id, request.getName(), request.getX(), request.getY());
        log.info("Node updated: {}", id);
        return ResponseEntity.ok(GraphNodeResponse.fromEntity(updated));
    }

    @DeleteMapping("/nodes/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable String id) {
        log.info("=== Node delete ===");
        log.info("id={}", id);
        service.deleteNode(id);
        log.info("Node deleted: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nodes/{id}")
    public ResponseEntity<GraphNodeResponse> getNode(@PathVariable String id) {
        GraphNodeEntity node = service.getNode(id);
        return ResponseEntity.ok(GraphNodeResponse.fromEntity(node));
    }

    @GetMapping("/nodes")
    public ResponseEntity<java.util.List<GraphNodeResponse>> getAllNodes() {
        GraphLoadResponse graph = service.load();
        java.util.List<GraphNodeResponse> nodes = graph.getNodes().stream()
                .map(dto -> {
                    GraphNodeResponse r = new GraphNodeResponse();
                    r.setId(dto.getId());
                    r.setName(dto.getName());
                    r.setX(dto.getX());
                    r.setY(dto.getY());
                    return r;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(nodes);
    }

    @PostMapping("/edges")
    public ResponseEntity<GraphEdgeResponse> addEdge(@Valid @RequestBody GraphEdgeRequest request) {
        log.info("=== Edge create ===");
        log.info("id={}, source={}, target={}, distance={}, traffic={}",
                request.getId(), request.getSource(), request.getTarget(), request.getDistance(), request.getTraffic());
        GraphEdgeEntity edge = new GraphEdgeEntity(request.getId(), request.getSource(),
                request.getTarget(), request.getDistance(), request.getTraffic());
        GraphEdgeEntity saved = service.addEdge(edge);
        log.info("Edge created: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(GraphEdgeResponse.fromEntity(saved));
    }

    @PutMapping("/edges/{id}")
    public ResponseEntity<GraphEdgeResponse> updateEdge(@PathVariable String id,
                                                          @Valid @RequestBody GraphEdgeUpdateRequest request) {
        log.info("=== Edge update ===");
        log.info("id={}, source={}, target={}, distance={}, traffic={}",
                id, request.getSource(), request.getTarget(), request.getDistance(), request.getTraffic());
        GraphEdgeEntity updated = service.updateEdge(id, request.getSource(),
                request.getTarget(), request.getDistance(), request.getTraffic());
        log.info("Edge updated: {}", id);
        return ResponseEntity.ok(GraphEdgeResponse.fromEntity(updated));
    }

    @DeleteMapping("/edges/{id}")
    public ResponseEntity<Void> deleteEdge(@PathVariable String id) {
        log.info("=== Edge delete ===");
        log.info("id={}", id);
        service.deleteEdge(id);
        log.info("Edge deleted: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/edges/{id}")
    public ResponseEntity<GraphEdgeResponse> getEdge(@PathVariable String id) {
        GraphEdgeEntity edge = service.getEdge(id);
        return ResponseEntity.ok(GraphEdgeResponse.fromEntity(edge));
    }
}
