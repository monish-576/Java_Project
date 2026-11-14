package com.ambulance.service;

import com.ambulance.dto.GraphSaveRequest;
import com.ambulance.dto.GraphLoadResponse;
import com.ambulance.model.GraphNodeEntity;
import com.ambulance.model.GraphEdgeEntity;
import com.ambulance.repository.GraphPersistenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GraphPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(GraphPersistenceService.class);
    private final GraphPersistenceRepository repository;

    public GraphPersistenceService(GraphPersistenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void save(GraphSaveRequest request) {
        List<GraphNodeEntity> nodes = request.getNodes().stream().map(dto -> {
            GraphNodeEntity n = new GraphNodeEntity();
            n.setId(dto.getId());
            n.setName(dto.getName());
            n.setX(dto.getX());
            n.setY(dto.getY());
            return n;
        }).collect(Collectors.toList());

        List<GraphEdgeEntity> edges = request.getEdges().stream().map(dto -> {
            GraphEdgeEntity e = new GraphEdgeEntity();
            e.setId(dto.getId());
            e.setSource(dto.getSource());
            e.setTarget(dto.getTarget());
            e.setDistance(dto.getDistance());
            e.setTraffic(dto.getTraffic() != null ? dto.getTraffic() : "normal");
            return e;
        }).collect(Collectors.toList());

        repository.replaceAll(nodes, edges);
        log.info("Graph saved: {} nodes, {} edges", nodes.size(), edges.size());
    }

    public GraphLoadResponse load() {
        List<GraphNodeEntity> nodes = repository.findAllNodes();
        List<GraphEdgeEntity> edges = repository.findAllEdges();
        log.info("Graph loaded: {} nodes, {} edges", nodes.size(), edges.size());
        return new GraphLoadResponse(nodes, edges);
    }

    @Transactional
    public GraphNodeEntity addNode(GraphNodeEntity node) {
        GraphNodeEntity saved = repository.saveNode(node);
        log.info("Node created: id={}, name={}, x={}, y={}", saved.getId(), saved.getName(), saved.getX(), saved.getY());
        return saved;
    }

    @Transactional
    public GraphNodeEntity updateNode(String id, String name, double x, double y) {
        GraphNodeEntity node = repository.findNodeById(id)
                .orElseThrow(() -> new RuntimeException("Node not found: " + id));
        node.setName(name);
        node.setX(x);
        node.setY(y);
        repository.updateNode(node);
        log.info("Node updated: id={}, name={}, x={}, y={}", id, name, x, y);
        return node;
    }

    @Transactional
    public void deleteNode(String id) {
        if (!repository.nodeExists(id)) {
            throw new RuntimeException("Node not found: " + id);
        }
        repository.deleteNodeById(id);
        log.info("Node deleted: id={}", id);
    }

    @Transactional
    public GraphEdgeEntity addEdge(GraphEdgeEntity edge) {
        GraphEdgeEntity saved = repository.saveEdge(edge);
        log.info("Edge created: id={}, source={}, target={}, distance={}, traffic={}",
                saved.getId(), saved.getSource(), saved.getTarget(), saved.getDistance(), saved.getTraffic());
        return saved;
    }

    @Transactional
    public GraphEdgeEntity updateEdge(String id, String source, String target, double distance, String traffic) {
        GraphEdgeEntity edge = repository.findEdgeById(id)
                .orElseThrow(() -> new RuntimeException("Edge not found: " + id));
        edge.setSource(source);
        edge.setTarget(target);
        edge.setDistance(distance);
        edge.setTraffic(traffic != null ? traffic : "normal");
        repository.updateEdge(edge);
        log.info("Edge updated: id={}, source={}, target={}, distance={}, traffic={}", id, source, target, distance, traffic);
        return edge;
    }

    @Transactional
    public void deleteEdge(String id) {
        if (!repository.edgeExists(id)) {
            throw new RuntimeException("Edge not found: " + id);
        }
        repository.deleteEdgeById(id);
        log.info("Edge deleted: id={}", id);
    }

    public GraphNodeEntity getNode(String id) {
        return repository.findNodeById(id)
                .orElseThrow(() -> new RuntimeException("Node not found: " + id));
    }

    public GraphEdgeEntity getEdge(String id) {
        return repository.findEdgeById(id)
                .orElseThrow(() -> new RuntimeException("Edge not found: " + id));
    }

    @Transactional
    public void clear() {
        repository.deleteAll();
        log.info("All graph data cleared");
    }
}
