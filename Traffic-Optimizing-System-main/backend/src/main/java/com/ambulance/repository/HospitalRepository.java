package com.ambulance.repository;

import com.ambulance.model.Hospital;
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
public class HospitalRepository {

    private static final Logger log = LoggerFactory.getLogger(HospitalRepository.class);
    private final JdbcTemplate jdbc;

    public HospitalRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Hospital save(Hospital hospital) {
        String id = hospital.getId() != null ? hospital.getId() : UUID.randomUUID().toString();
        String sql = """
            INSERT INTO hospitals (id, name, address, latitude, longitude, phone, emergency_level)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                address = VALUES(address),
                latitude = VALUES(latitude),
                longitude = VALUES(longitude),
                phone = VALUES(phone),
                emergency_level = VALUES(emergency_level)
            """;
        jdbc.update(sql, id, hospital.getName(), hospital.getAddress(),
                hospital.getLatitude(), hospital.getLongitude(),
                hospital.getPhone(), hospital.getEmergencyLevel());
        hospital.setId(id);
        log.debug("Saved hospital: {} ({})", hospital.getName(), id);
        return hospital;
    }

    public List<Hospital> findAll() {
        return jdbc.query("SELECT * FROM hospitals ORDER BY name", this::mapRow);
    }

    public Optional<Hospital> findById(String id) {
        return jdbc.query("SELECT * FROM hospitals WHERE id = ?", this::mapRow, id).stream().findFirst();
    }

    public void update(Hospital hospital) {
        String sql = """
            UPDATE hospitals SET name = ?, address = ?, latitude = ?, longitude = ?,
                phone = ?, emergency_level = ?
            WHERE id = ?
            """;
        jdbc.update(sql, hospital.getName(), hospital.getAddress(),
                hospital.getLatitude(), hospital.getLongitude(),
                hospital.getPhone(), hospital.getEmergencyLevel(),
                hospital.getId());
        log.debug("Updated hospital: {}", hospital.getId());
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM hospitals WHERE id = ?", id);
        log.debug("Deleted hospital: {}", id);
    }

    public boolean existsById(String id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM hospitals WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    private Hospital mapRow(ResultSet rs, int rowNum) throws SQLException {
        Hospital h = new Hospital();
        h.setId(rs.getString("id"));
        h.setName(rs.getString("name"));
        h.setAddress(rs.getString("address"));
        h.setLatitude(rs.getDouble("latitude"));
        h.setLongitude(rs.getDouble("longitude"));
        h.setPhone(rs.getString("phone"));
        h.setEmergencyLevel(rs.getString("emergency_level"));
        if (rs.getTimestamp("created_at") != null)
            h.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null)
            h.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return h;
    }
}
