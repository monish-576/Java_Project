package com.ambulance.repository;

import com.ambulance.model.GraphNodeEntity;
import com.ambulance.model.GraphEdgeEntity;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GraphPersistenceRepository {

    private static final Logger log = LoggerFactory.getLogger(GraphPersistenceRepository.class);
    private final JdbcTemplate jdbc;

    public GraphPersistenceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void initTables() {
        try {
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS graph_nodes (
                    id          VARCHAR(36)     NOT NULL PRIMARY KEY,
                    name        VARCHAR(255)    NOT NULL,
                    x           DOUBLE          NOT NULL DEFAULT 0,
                    y           DOUBLE          NOT NULL DEFAULT 0,
                    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_node_name (name)
                ) ENGINE=InnoDB
            """);
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS graph_edges (
                    id          VARCHAR(36)     NOT NULL PRIMARY KEY,
                    source      VARCHAR(36)     NOT NULL,
                    target      VARCHAR(36)     NOT NULL,
                    distance    DOUBLE          NOT NULL CHECK (distance > 0),
                    traffic     VARCHAR(20)     NOT NULL DEFAULT 'normal',
                    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_edge_source (source),
                    INDEX idx_edge_target (target),
                    INDEX idx_edge_traffic (traffic)
                ) ENGINE=InnoDB
            """);
            log.info("Ensured graph_nodes and graph_edges tables exist");
        } catch (Exception e) {
            log.warn("Could not create graph tables (may already exist or DB unavailable): {}", e.getMessage());
        }
    }

    public List<GraphNodeEntity> findAllNodes() {
        return jdbc.query("SELECT * FROM graph_nodes ORDER BY name", this::mapNodeRow);
    }

    public List<GraphEdgeEntity> findAllEdges() {
        return jdbc.query("SELECT * FROM graph_edges ORDER BY source, target", this::mapEdgeRow);
    }

    public Optional<GraphNodeEntity> findNodeById(String id) {
        return jdbc.query("SELECT * FROM graph_nodes WHERE id = ?", this::mapNodeRow, id).stream().findFirst();
    }

    public Optional<GraphEdgeEntity> findEdgeById(String id) {
        return jdbc.query("SELECT * FROM graph_edges WHERE id = ?", this::mapEdgeRow, id).stream().findFirst();
    }

    @Transactional
    public void replaceAll(List<GraphNodeEntity> nodes, List<GraphEdgeEntity> edges) {
        jdbc.update("DELETE FROM graph_edges");
        jdbc.update("DELETE FROM graph_nodes");

        for (GraphNodeEntity node : nodes) {
            jdbc.update(
                "INSERT INTO graph_nodes (id, name, x, y) VALUES (?, ?, ?, ?)",
                node.getId(), node.getName(), node.getX(), node.getY()
            );
        }

        for (GraphEdgeEntity edge : edges) {
            jdbc.update(
                "INSERT INTO graph_edges (id, source, target, distance, traffic) VALUES (?, ?, ?, ?, ?)",
                edge.getId(), edge.getSource(), edge.getTarget(), edge.getDistance(), edge.getTraffic()
            );
        }

        log.info("Saved graph: {} nodes, {} edges", nodes.size(), edges.size());
    }

    @Transactional
    public GraphNodeEntity saveNode(GraphNodeEntity node) {
        String id = node.getId() != null ? node.getId() : UUID.randomUUID().toString();
        jdbc.update(
            "INSERT INTO graph_nodes (id, name, x, y) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name), x = VALUES(x), y = VALUES(y)",
            id, node.getName(), node.getX(), node.getY()
        );
        node.setId(id);
        log.info("Saved node: id={}, name={}, x={}, y={}", id, node.getName(), node.getX(), node.getY());
        return node;
    }

    @Transactional
    public GraphEdgeEntity saveEdge(GraphEdgeEntity edge) {
        String id = edge.getId() != null ? edge.getId() : UUID.randomUUID().toString();
        jdbc.update(
            "INSERT INTO graph_edges (id, source, target, distance, traffic) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE source = VALUES(source), target = VALUES(target), distance = VALUES(distance), traffic = VALUES(traffic)",
            id, edge.getSource(), edge.getTarget(), edge.getDistance(), edge.getTraffic()
        );
        edge.setId(id);
        log.info("Saved edge: id={}, source={}, target={}, distance={}, traffic={}", id, edge.getSource(), edge.getTarget(), edge.getDistance(), edge.getTraffic());
        return edge;
    }

    @Transactional
    public void updateNode(GraphNodeEntity node) {
        jdbc.update(
            "UPDATE graph_nodes SET name = ?, x = ?, y = ? WHERE id = ?",
            node.getName(), node.getX(), node.getY(), node.getId()
        );
        log.info("Updated node: id={}, name={}, x={}, y={}", node.getId(), node.getName(), node.getX(), node.getY());
    }

    @Transactional
    public void updateEdge(GraphEdgeEntity edge) {
        jdbc.update(
            "UPDATE graph_edges SET source = ?, target = ?, distance = ?, traffic = ? WHERE id = ?",
            edge.getSource(), edge.getTarget(), edge.getDistance(), edge.getTraffic(), edge.getId()
        );
        log.info("Updated edge: id={}, source={}, target={}, distance={}, traffic={}", edge.getId(), edge.getSource(), edge.getTarget(), edge.getDistance(), edge.getTraffic());
    }

    @Transactional
    public void deleteNodeById(String id) {
        jdbc.update("DELETE FROM graph_edges WHERE source = ? OR target = ?", id, id);
        jdbc.update("DELETE FROM graph_nodes WHERE id = ?", id);
        log.info("Deleted node and its edges: id={}", id);
    }

    @Transactional
    public void deleteEdgeById(String id) {
        jdbc.update("DELETE FROM graph_edges WHERE id = ?", id);
        log.info("Deleted edge: id={}", id);
    }

    public void deleteAll() {
        jdbc.update("DELETE FROM graph_edges");
        jdbc.update("DELETE FROM graph_nodes");
        log.info("Cleared all graph data");
    }

    public boolean nodeExists(String id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM graph_nodes WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public boolean edgeExists(String id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM graph_edges WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    private GraphNodeEntity mapNodeRow(ResultSet rs, int rowNum) throws SQLException {
        GraphNodeEntity n = new GraphNodeEntity();
        n.setId(rs.getString("id"));
        n.setName(rs.getString("name"));
        n.setX(rs.getDouble("x"));
        n.setY(rs.getDouble("y"));
        if (rs.getTimestamp("created_at") != null)
            n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null)
            n.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return n;
    }

    private GraphEdgeEntity mapEdgeRow(ResultSet rs, int rowNum) throws SQLException {
        GraphEdgeEntity e = new GraphEdgeEntity();
        e.setId(rs.getString("id"));
        e.setSource(rs.getString("source"));
        e.setTarget(rs.getString("target"));
        e.setDistance(rs.getDouble("distance"));
        e.setTraffic(rs.getString("traffic"));
        if (rs.getTimestamp("created_at") != null)
            e.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null)
            e.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return e;
    }
}
