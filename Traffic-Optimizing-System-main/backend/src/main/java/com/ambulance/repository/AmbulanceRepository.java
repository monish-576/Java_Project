package com.ambulance.repository;

import com.ambulance.model.Ambulance;
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
public class AmbulanceRepository {

    private static final Logger log = LoggerFactory.getLogger(AmbulanceRepository.class);
    private final JdbcTemplate jdbc;

    public AmbulanceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Ambulance save(Ambulance ambulance) {
        String id = ambulance.getId() != null ? ambulance.getId() : UUID.randomUUID().toString();
        String sql = """
            INSERT INTO ambulances (id, hospital_id, vehicle_number, driver_name, driver_phone, status)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                hospital_id = VALUES(hospital_id),
                vehicle_number = VALUES(vehicle_number),
                driver_name = VALUES(driver_name),
                driver_phone = VALUES(driver_phone),
                status = VALUES(status)
            """;
        jdbc.update(sql, id, ambulance.getHospitalId(), ambulance.getVehicleNumber(),
                ambulance.getDriverName(), ambulance.getDriverPhone(), ambulance.getStatus());
        ambulance.setId(id);
        log.debug("Saved ambulance: {} ({})", ambulance.getVehicleNumber(), id);
        return ambulance;
    }

    public List<Ambulance> findAll() {
        return jdbc.query("SELECT * FROM ambulances ORDER BY vehicle_number", this::mapRow);
    }

    public Optional<Ambulance> findById(String id) {
        return jdbc.query("SELECT * FROM ambulances WHERE id = ?", this::mapRow, id).stream().findFirst();
    }

    public List<Ambulance> findByHospitalId(String hospitalId) {
        return jdbc.query("SELECT * FROM ambulances WHERE hospital_id = ? ORDER BY vehicle_number",
                this::mapRow, hospitalId);
    }

    public List<Ambulance> findByStatus(String status) {
        return jdbc.query("SELECT * FROM ambulances WHERE status = ?", this::mapRow, status);
    }

    public void update(Ambulance ambulance) {
        String sql = """
            UPDATE ambulances SET hospital_id = ?, vehicle_number = ?, driver_name = ?,
                driver_phone = ?, status = ?, current_lat = ?, current_lng = ?
            WHERE id = ?
            """;
        jdbc.update(sql, ambulance.getHospitalId(), ambulance.getVehicleNumber(),
                ambulance.getDriverName(), ambulance.getDriverPhone(), ambulance.getStatus(),
                ambulance.getCurrentLat(), ambulance.getCurrentLng(), ambulance.getId());
        log.debug("Updated ambulance: {}", ambulance.getId());
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM ambulances WHERE id = ?", id);
        log.debug("Deleted ambulance: {}", id);
    }

    public boolean existsById(String id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM ambulances WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    private Ambulance mapRow(ResultSet rs, int rowNum) throws SQLException {
        Ambulance a = new Ambulance();
        a.setId(rs.getString("id"));
        a.setHospitalId(rs.getString("hospital_id"));
        a.setVehicleNumber(rs.getString("vehicle_number"));
        a.setDriverName(rs.getString("driver_name"));
        a.setDriverPhone(rs.getString("driver_phone"));
        a.setStatus(rs.getString("status"));
        double lat = rs.getDouble("current_lat");
        if (!rs.wasNull()) a.setCurrentLat(lat);
        double lng = rs.getDouble("current_lng");
        if (!rs.wasNull()) a.setCurrentLng(lng);
        if (rs.getTimestamp("last_updated") != null)
            a.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
        if (rs.getTimestamp("created_at") != null)
            a.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return a;
    }
}
