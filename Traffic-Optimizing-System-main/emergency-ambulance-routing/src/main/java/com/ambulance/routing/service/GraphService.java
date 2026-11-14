package com.ambulance.routing.service;

import com.ambulance.routing.model.Edge;
import com.ambulance.routing.model.Graph;
import com.ambulance.routing.model.Node;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class GraphService {

    private final JdbcTemplate jdbcTemplate;
    private Graph graph;

    public GraphService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        graph = new Graph("road-graph-001", "City Road Network");

        jdbcTemplate.query("SELECT id, name, node_type, x_coord, y_coord FROM nodes", this::mapNode)
                .forEach(graph::addNode);

        jdbcTemplate.query("SELECT id, source_id, target_id, distance, traffic FROM edges", this::mapEdge)
                .forEach(graph::addEdge);
    }

    private Node mapNode(ResultSet rs, int rowNum) throws SQLException {
        return new Node(
                rs.getString("id"),
                rs.getString("name"),
                Node.NodeType.valueOf(rs.getString("node_type")),
                rs.getInt("x_coord"),
                rs.getInt("y_coord")
        );
    }

    private Edge mapEdge(ResultSet rs, int rowNum) throws SQLException {
        return new Edge(
                rs.getString("id"),
                rs.getString("source_id"),
                rs.getString("target_id"),
                rs.getDouble("distance"),
                Edge.TrafficLevel.valueOf(rs.getString("traffic"))
        );
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Node getNodeById(String nodeId) {
        return graph.getNodeById(nodeId);
    }

    public boolean hasNode(String nodeId) {
        return graph.hasNode(nodeId);
    }
}
