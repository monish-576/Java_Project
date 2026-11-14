-- ============================================================
-- Smart Route Optimization System - Database Schema
-- Database: ambulance_routing
-- ============================================================

CREATE DATABASE IF NOT EXISTS ambulance_routing
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ambulance_routing;

-- ------------------------------------------------------------
-- Table: graph_nodes
-- Stores all graph vertices/nodes for route optimization
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS graph_nodes (
    id          VARCHAR(36)     NOT NULL PRIMARY KEY,
    name        VARCHAR(255)    NOT NULL,
    x           DOUBLE          NOT NULL DEFAULT 0,
    y           DOUBLE          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_node_name (name)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: graph_edges
-- Stores connections between nodes with distance and traffic info
-- ------------------------------------------------------------
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
    INDEX idx_edge_traffic (traffic),
    CONSTRAINT fk_edge_source FOREIGN KEY (source) REFERENCES graph_nodes(id) ON DELETE CASCADE,
    CONSTRAINT fk_edge_target FOREIGN KEY (target) REFERENCES graph_nodes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: route_history
-- Records every algorithm execution with full metadata
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS route_history (
    id                  VARCHAR(36)     NOT NULL PRIMARY KEY,
    source_node         VARCHAR(36)     NOT NULL,
    destination_node    VARCHAR(36)     NOT NULL,
    algorithm           VARCHAR(20)     NOT NULL,
    distance_km         DOUBLE          NOT NULL,
    path_json           JSON            NOT NULL COMMENT 'Ordered list of node IDs',
    visited_count       INT             NOT NULL DEFAULT 0,
    execution_time_ms   BIGINT          NOT NULL DEFAULT 0,
    traffic_conditions  JSON            DEFAULT NULL,
    estimated_travel_time_min DOUBLE    DEFAULT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_history_source (source_node),
    INDEX idx_history_dest (destination_node),
    INDEX idx_history_algo (algorithm),
    INDEX idx_history_time (created_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: hospitals
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hospitals (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    address         TEXT            DEFAULT NULL,
    latitude        DOUBLE          NOT NULL DEFAULT 0,
    longitude       DOUBLE          NOT NULL DEFAULT 0,
    phone           VARCHAR(20)     DEFAULT NULL,
    emergency_level VARCHAR(20)     NOT NULL DEFAULT 'general',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_hospital_name (name),
    INDEX idx_hospital_level (emergency_level)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: ambulances
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ambulances (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    hospital_id     VARCHAR(36)     NOT NULL,
    vehicle_number  VARCHAR(50)     NOT NULL,
    driver_name     VARCHAR(255)    DEFAULT NULL,
    driver_phone    VARCHAR(20)     DEFAULT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
    current_lat     DOUBLE          DEFAULT NULL,
    current_lng     DOUBLE          DEFAULT NULL,
    last_updated    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_ambulance_status (status),
    INDEX idx_ambulance_hospital (hospital_id),
    CONSTRAINT fk_ambulance_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: traffic_conditions
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS traffic_conditions (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    road_id         VARCHAR(36)     NOT NULL,
    congestion_level VARCHAR(20)    NOT NULL DEFAULT 'NORMAL',
    avg_speed_kmh   DOUBLE          DEFAULT NULL,
    recorded_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source          VARCHAR(100)    DEFAULT 'system',

    INDEX idx_traffic_road (road_id),
    INDEX idx_traffic_time (recorded_at),
    INDEX idx_traffic_level (congestion_level)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: accident_locations
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accident_locations (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    node_id         VARCHAR(36)     DEFAULT NULL,
    latitude        DOUBLE          NOT NULL,
    longitude       DOUBLE          NOT NULL,
    severity        VARCHAR(20)     NOT NULL DEFAULT 'MODERATE',
    description     TEXT            DEFAULT NULL,
    reported_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cleared_at      TIMESTAMP       DEFAULT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,

    INDEX idx_accident_active (is_active),
    INDEX idx_accident_severity (severity),
    INDEX idx_accident_time (reported_at)
) ENGINE=InnoDB;
