package com.ambulance.repository;

import com.ambulance.model.RoadNetwork;
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
public class RoadNetworkRepository {

    private static final Logger log = LoggerFactory.getLogger(RoadNetworkRepository.class);
    private final JdbcTemplate jdbc;

    public RoadNetworkRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public RoadNetwork save(RoadNetwork road) {
        String id = road.getId() != null ? road.getId() : UUID.randomUUID().toString();
        String sql = """
            INSERT INTO road_network (id, source_node, target_node, distance_km,
                road_name, road_type, speed_limit, is_one_way)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                source_node = VALUES(source_node),
                target_node = VALUES(target_node),
                distance_km = VALUES(distance_km),
                road_name = VALUES(road_name),
                road_type = VALUES(road_type),
                speed_limit = VALUES(speed_limit),
                is_one_way = VALUES(is_one_way)
            """;
        jdbc.update(sql, id, road.getSourceNode(), road.getTargetNode(),
                road.getDistanceKm(), road.getRoadName(), road.getRoadType(),
                road.getSpeedLimit(), road.isOneWay());
        road.setId(id);
        log.debug("Saved road: {} -> {} ({} km)", road.getSourceNode(), road.getTargetNode(), road.getDistanceKm());
        return road;
    }

    public List<RoadNetwork> findAll() {
        return jdbc.query("SELECT * FROM road_network ORDER BY road_name", this::mapRow);
    }

    public Optional<RoadNetwork> findById(String id) {
        return jdbc.query("SELECT * FROM road_network WHERE id = ?", this::mapRow, id).stream().findFirst();
    }

    public List<RoadNetwork> findBySource(String sourceNode) {
        return jdbc.query("SELECT * FROM road_network WHERE source_node = ?", this::mapRow, sourceNode);
    }

    public List<RoadNetwork> findByTarget(String targetNode) {
        return jdbc.query("SELECT * FROM road_network WHERE target_node = ?", this::mapRow, targetNode);
    }

    public List<RoadNetwork> findByType(String roadType) {
        return jdbc.query("SELECT * FROM road_network WHERE road_type = ?", this::mapRow, roadType);
    }

    public void update(RoadNetwork road) {
        String sql = """
            UPDATE road_network SET source_node = ?, target_node = ?, distance_km = ?,
                road_name = ?, road_type = ?, speed_limit = ?, is_one_way = ?
            WHERE id = ?
            """;
        jdbc.update(sql, road.getSourceNode(), road.getTargetNode(), road.getDistanceKm(),
                road.getRoadName(), road.getRoadType(), road.getSpeedLimit(),
                road.isOneWay(), road.getId());
        log.debug("Updated road: {}", road.getId());
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM road_network WHERE id = ?", id);
        log.debug("Deleted road: {}", id);
    }

    public boolean existsById(String id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM road_network WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    private RoadNetwork mapRow(ResultSet rs, int rowNum) throws SQLException {
        RoadNetwork r = new RoadNetwork();
        r.setId(rs.getString("id"));
        r.setSourceNode(rs.getString("source_node"));
        r.setTargetNode(rs.getString("target_node"));
        r.setDistanceKm(rs.getDouble("distance_km"));
        r.setRoadName(rs.getString("road_name"));
        r.setRoadType(rs.getString("road_type"));
        r.setSpeedLimit(rs.getInt("speed_limit"));
        r.setOneWay(rs.getBoolean("is_one_way"));
        if (rs.getTimestamp("created_at") != null)
            r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null)
            r.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return r;
    }
}
