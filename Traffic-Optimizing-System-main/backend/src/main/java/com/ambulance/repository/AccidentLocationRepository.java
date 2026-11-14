package com.ambulance.repository;

import com.ambulance.model.AccidentLocation;
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
public class AccidentLocationRepository {

    private static final Logger log = LoggerFactory.getLogger(AccidentLocationRepository.class);
    private final JdbcTemplate jdbc;

    public AccidentLocationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AccidentLocation save(AccidentLocation location) {
        String id = location.getId() != null ? location.getId() : UUID.randomUUID().toString();
        String sql = """
            INSERT INTO accident_locations (id, node_id, latitude, longitude, severity, description)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                node_id = VALUES(node_id),
                latitude = VALUES(latitude),
                longitude = VALUES(longitude),
                severity = VALUES(severity),
                description = VALUES(description)
            """;
        jdbc.update(sql, id, location.getNodeId(), location.getLatitude(),
                location.getLongitude(), location.getSeverity(), location.getDescription());
        location.setId(id);
        log.debug("Saved accident location: {} ({})", location.getSeverity(), id);
        return location;
    }

    public List<AccidentLocation> findAll() {
        return jdbc.query("SELECT * FROM accident_locations ORDER BY reported_at DESC", this::mapRow);
    }

    public List<AccidentLocation> findActive() {
        return jdbc.query(
                "SELECT * FROM accident_locations WHERE is_active = TRUE ORDER BY reported_at DESC",
                this::mapRow);
    }

    public Optional<AccidentLocation> findById(String id) {
        return jdbc.query("SELECT * FROM accident_locations WHERE id = ?", this::mapRow, id)
                .stream().findFirst();
    }

    public void update(AccidentLocation location) {
        String sql = """
            UPDATE accident_locations SET node_id = ?, latitude = ?, longitude = ?,
                severity = ?, description = ?, is_active = ?, cleared_at = ?
            WHERE id = ?
            """;
        jdbc.update(sql, location.getNodeId(), location.getLatitude(), location.getLongitude(),
                location.getSeverity(), location.getDescription(), location.isActive(),
                location.getClearedAt(), location.getId());
        log.debug("Updated accident location: {}", location.getId());
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM accident_locations WHERE id = ?", id);
        log.debug("Deleted accident location: {}", id);
    }

    public boolean existsById(String id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM accident_locations WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    private AccidentLocation mapRow(ResultSet rs, int rowNum) throws SQLException {
        AccidentLocation loc = new AccidentLocation();
        loc.setId(rs.getString("id"));
        loc.setNodeId(rs.getString("node_id"));
        loc.setLatitude(rs.getDouble("latitude"));
        loc.setLongitude(rs.getDouble("longitude"));
        loc.setSeverity(rs.getString("severity"));
        loc.setDescription(rs.getString("description"));
        loc.setActive(rs.getBoolean("is_active"));
        if (rs.getTimestamp("reported_at") != null)
            loc.setReportedAt(rs.getTimestamp("reported_at").toLocalDateTime());
        if (rs.getTimestamp("cleared_at") != null)
            loc.setClearedAt(rs.getTimestamp("cleared_at").toLocalDateTime());
        return loc;
    }
}
