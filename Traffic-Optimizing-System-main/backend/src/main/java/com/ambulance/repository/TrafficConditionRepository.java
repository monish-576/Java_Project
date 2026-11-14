package com.ambulance.repository;

import com.ambulance.model.TrafficCondition;
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
public class TrafficConditionRepository {

    private static final Logger log = LoggerFactory.getLogger(TrafficConditionRepository.class);
    private final JdbcTemplate jdbc;

    public TrafficConditionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public TrafficCondition save(TrafficCondition condition) {
        String id = condition.getId() != null ? condition.getId() : UUID.randomUUID().toString();
        String sql = """
            INSERT INTO traffic_conditions (id, road_id, congestion_level, avg_speed_kmh, source)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                road_id = VALUES(road_id),
                congestion_level = VALUES(congestion_level),
                avg_speed_kmh = VALUES(avg_speed_kmh),
                source = VALUES(source)
            """;
        jdbc.update(sql, id, condition.getRoadId(), condition.getCongestionLevel(),
                condition.getAvgSpeedKmh(), condition.getSource());
        condition.setId(id);
        log.debug("Saved traffic condition: {} ({})", condition.getRoadId(), id);
        return condition;
    }

    public List<TrafficCondition> findAll() {
        return jdbc.query("SELECT * FROM traffic_conditions ORDER BY recorded_at DESC", this::mapRow);
    }

    public Optional<TrafficCondition> findById(String id) {
        return jdbc.query("SELECT * FROM traffic_conditions WHERE id = ?", this::mapRow, id)
                .stream().findFirst();
    }

    public List<TrafficCondition> findRecentByRoadId(String roadId) {
        String sql = """
            SELECT * FROM traffic_conditions
            WHERE road_id = ? ORDER BY recorded_at DESC LIMIT 10
            """;
        return jdbc.query(sql, this::mapRow, roadId);
    }

    public List<TrafficCondition> findActiveCongestion() {
        String sql = """
            SELECT * FROM traffic_conditions
            WHERE congestion_level IN ('HEAVY', 'BLOCKED')
            AND recorded_at > NOW() - INTERVAL 1 HOUR
            ORDER BY recorded_at DESC
            """;
        return jdbc.query(sql, this::mapRow);
    }

    public void update(TrafficCondition condition) {
        String sql = """
            UPDATE traffic_conditions SET road_id = ?, congestion_level = ?,
                avg_speed_kmh = ?, source = ?
            WHERE id = ?
            """;
        jdbc.update(sql, condition.getRoadId(), condition.getCongestionLevel(),
                condition.getAvgSpeedKmh(), condition.getSource(), condition.getId());
        log.debug("Updated traffic condition: {}", condition.getId());
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM traffic_conditions WHERE id = ?", id);
        log.debug("Deleted traffic condition: {}", id);
    }

    public boolean existsById(String id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM traffic_conditions WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    private TrafficCondition mapRow(ResultSet rs, int rowNum) throws SQLException {
        TrafficCondition tc = new TrafficCondition();
        tc.setId(rs.getString("id"));
        tc.setRoadId(rs.getString("road_id"));
        tc.setCongestionLevel(rs.getString("congestion_level"));
        double speed = rs.getDouble("avg_speed_kmh");
        if (!rs.wasNull()) tc.setAvgSpeedKmh(speed);
        if (rs.getTimestamp("recorded_at") != null)
            tc.setRecordedAt(rs.getTimestamp("recorded_at").toLocalDateTime());
        tc.setSource(rs.getString("source"));
        return tc;
    }
}
