package com.ambulance.routing.service;

import com.ambulance.routing.algorithm.AStarAlgorithm;
import com.ambulance.routing.algorithm.BFSAlgorithm;
import com.ambulance.routing.algorithm.DFSAlgorithm;
import com.ambulance.routing.algorithm.DijkstraAlgorithm;
import com.ambulance.routing.model.AlgorithmResult;
import org.springframework.stereotype.Service;

@Service
public class PathFindingService {

    private final DijkstraAlgorithm dijkstra;
    private final BFSAlgorithm bfs;
    private final DFSAlgorithm dfs;
    private final AStarAlgorithm aStar;
    private final GraphService graphService;

    public PathFindingService(DijkstraAlgorithm dijkstra, BFSAlgorithm bfs,
                              DFSAlgorithm dfs, AStarAlgorithm aStar,
                              GraphService graphService) {
        this.dijkstra = dijkstra;
        this.bfs = bfs;
        this.dfs = dfs;
        this.aStar = aStar;
        this.graphService = graphService;
    }

    public AlgorithmResult runDijkstra(String sourceId, String destinationId) {
        return dijkstra.findPath(graphService.getGraph(), sourceId, destinationId);
    }

    public AlgorithmResult runBfs(String sourceId, String destinationId) {
        return bfs.findPath(graphService.getGraph(), sourceId, destinationId);
    }

    public AlgorithmResult runDfs(String sourceId, String destinationId) {
        return dfs.findPath(graphService.getGraph(), sourceId, destinationId);
    }

    public AlgorithmResult runAStar(String sourceId, String destinationId) {
        return aStar.findPath(graphService.getGraph(), sourceId, destinationId);
    }
}
