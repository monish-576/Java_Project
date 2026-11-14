package com.ambulance.routing.controller;

import com.ambulance.routing.dto.AlgorithmResponse;
import com.ambulance.routing.model.AlgorithmResult;
import com.ambulance.routing.service.PathFindingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/algorithms")
public class AlgorithmController {

    private final PathFindingService pathFindingService;

    public AlgorithmController(PathFindingService pathFindingService) {
        this.pathFindingService = pathFindingService;
    }

    @GetMapping("/dijkstra")
    public ResponseEntity<AlgorithmResponse> dijkstra(
            @RequestParam String sourceId,
            @RequestParam String destinationId) {
        AlgorithmResult result = pathFindingService.runDijkstra(sourceId, destinationId);
        return ResponseEntity.ok(new AlgorithmResponse("Dijkstra", result));
    }

    @GetMapping("/bfs")
    public ResponseEntity<AlgorithmResponse> bfs(
            @RequestParam String sourceId,
            @RequestParam String destinationId) {
        AlgorithmResult result = pathFindingService.runBfs(sourceId, destinationId);
        return ResponseEntity.ok(new AlgorithmResponse("BFS", result));
    }

    @GetMapping("/dfs")
    public ResponseEntity<AlgorithmResponse> dfs(
            @RequestParam String sourceId,
            @RequestParam String destinationId) {
        AlgorithmResult result = pathFindingService.runDfs(sourceId, destinationId);
        return ResponseEntity.ok(new AlgorithmResponse("DFS", result));
    }

    @GetMapping("/astar")
    public ResponseEntity<AlgorithmResponse> astar(
            @RequestParam String sourceId,
            @RequestParam String destinationId) {
        AlgorithmResult result = pathFindingService.runAStar(sourceId, destinationId);
        return ResponseEntity.ok(new AlgorithmResponse("A*", result));
    }
}
