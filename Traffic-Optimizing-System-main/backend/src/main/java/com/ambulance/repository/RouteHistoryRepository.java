package com.ambulance.repository;

import com.ambulance.model.RouteHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RouteHistoryRepository {

    private static final Logger log = LoggerFactory.getLogger(RouteHistoryRepository.class);
    private final JdbcTemplate jdbc;

    public RouteHistoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public RouteHistory save(RouteHistory history) {
        String id = history.getId() != null ? history.getId() : UUID.randomUUID().toString();
        String sql = """
            INSERT INTO route_history (id, source_node, destination_node, algorithm,
                distance_km, path_json, visited_count, execution_time_ms,
                estimated_travel_time_min)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                source_node = VALUES(source_node),
                destination_node = VALUES(destination_node),
                algorithm = VALUES(algorithm),
                distance_km = VALUES(distance_km),
                path_json = VALUES(path_json),
                visited_count = VALUES(visited_count),
                execution_time_ms = VALUES(execution_time_ms),
                estimated_travel_time_min = VALUES(estimated_travel_time_min)
            """;
        jdbc.update(sql, id, history.getSourceNode(), history.getDestinationNode(),
                history.getAlgorithm(), history.getDistanceKm(), history.getPathJson(),
                history.getVisitedCount(), history.getExecutionTimeMs(),
                history.getEstimatedTravelTimeMin());
        history.setId(id);
        log.debug("Saved route history: {}", id);
        return history;
    }

    public List<RouteHistory> findAll() {
        String sql = "SELECT * FROM route_history ORDER BY created_at DESC LIMIT 100";
        return jdbc.query(sql, this::mapRow);
    }

    public Optional<RouteHistory> findById(String id) {
        String sql = "SELECT * FROM route_history WHERE id = ?";
        return jdbc.query(sql, this::mapRow, id).stream().findFirst();
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM route_history WHERE id = ?", id);
        log.debug("Deleted route history: {}", id);
    }

    public boolean existsById(String id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM route_history WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    private RouteHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
        RouteHistory rh = new RouteHistory();
        rh.setId(rs.getString("id"));
        rh.setSourceNode(rs.getString("source_node"));
        rh.setDestinationNode(rs.getString("destination_node"));
        rh.setAlgorithm(rs.getString("algorithm"));
        rh.setDistanceKm(rs.getDouble("distance_km"));
        rh.setPathJson(rs.getString("path_json"));
        rh.setVisitedCount(rs.getInt("visited_count"));
        rh.setExecutionTimeMs(rs.getLong("execution_time_ms"));
        rh.setTrafficConditions(rs.getString("traffic_conditions"));
        double estTime = rs.getDouble("estimated_travel_time_min");
        if (!rs.wasNull()) rh.setEstimatedTravelTimeMin(estTime);
        if (rs.getTimestamp("created_at") != null)
            rh.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return rh;
    }
}
