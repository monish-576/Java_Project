-- ============================================================
-- Emergency Ambulance Routing System - Database Schema
-- Database: ambulance_routing
-- ============================================================

CREATE DATABASE IF NOT EXISTS ambulance_routing
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ambulance_routing;

-- ------------------------------------------------------------
-- Table: nodes
-- Stores all graph vertices (locations, hospitals, etc.)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS nodes (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    node_type       ENUM('REGULAR', 'HOSPITAL', 'ACCIDENT', 'AMBULANCE_DEPOT')
                                    NOT NULL DEFAULT 'REGULAR',
    x_coord         INT             NOT NULL DEFAULT 0,
    y_coord         INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_node_type (node_type),
    INDEX idx_node_name (name)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: edges
-- Stores weighted directed/undirected edges (roads)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS edges (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    graph_id        VARCHAR(36)     NOT NULL DEFAULT 'default',
    source_id       VARCHAR(36)     NOT NULL,
    target_id       VARCHAR(36)     NOT NULL,
    distance        DOUBLE          NOT NULL CHECK (distance > 0),
    traffic         ENUM('NORMAL', 'MODERATE', 'HEAVY', 'BLOCKED')
                                    NOT NULL DEFAULT 'NORMAL',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_edge_source (source_id),
    INDEX idx_edge_target (target_id),
    INDEX idx_edge_graph (graph_id),
    CONSTRAINT fk_edge_source FOREIGN KEY (source_id) REFERENCES nodes(id) ON DELETE CASCADE,
    CONSTRAINT fk_edge_target FOREIGN KEY (target_id) REFERENCES nodes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: shortest_paths
-- Stores results of shortest path computations
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shortest_paths (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    graph_id        VARCHAR(36)     NOT NULL DEFAULT 'default',
    source_id       VARCHAR(36)     NOT NULL,
    destination_id  VARCHAR(36)     NOT NULL,
    algorithm       ENUM('DIJKSTRA', 'BFS', 'DFS', 'ASTAR')
                                    NOT NULL,
    distance        DOUBLE          NOT NULL,
    path_json       JSON            NOT NULL COMMENT 'Ordered list of node IDs as JSON array',
    visited_count   INT             NOT NULL DEFAULT 0,
    execution_time_ms BIGINT        NOT NULL DEFAULT 0,
    traffic_conditions JSON         DEFAULT NULL COMMENT 'Edge traffic levels at computation time',
    computed_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_path_source (source_id),
    INDEX idx_path_dest (destination_id),
    INDEX idx_path_algorithm (algorithm),
    INDEX idx_path_computed (computed_at),
    CONSTRAINT fk_path_source FOREIGN KEY (source_id) REFERENCES nodes(id) ON DELETE CASCADE,
    CONSTRAINT fk_path_dest FOREIGN KEY (destination_id) REFERENCES nodes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: graph_history
-- Stores snapshots of previously saved/loaded graphs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS graph_history (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT            DEFAULT NULL,
    node_count      INT             NOT NULL DEFAULT 0,
    edge_count      INT             NOT NULL DEFAULT 0,
    graph_json      JSON            NOT NULL COMMENT 'Full graph snapshot as JSON',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_history_name (name),
    INDEX idx_history_created (created_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: ambulance_runs
-- Records emergency dispatch runs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ambulance_runs (
    id                  VARCHAR(36)     NOT NULL PRIMARY KEY,
    hospital_id         VARCHAR(36)     NOT NULL,
    accident_node_id    VARCHAR(36)     NOT NULL,
    route_path_json     JSON            NOT NULL,
    total_distance_km   DOUBLE          NOT NULL,
    eta_minutes         DOUBLE          NOT NULL COMMENT 'Estimated Time of Arrival in minutes',
    avg_speed_kmh       DOUBLE          NOT NULL DEFAULT 40.0,
    traffic_condition   ENUM('NORMAL', 'MODERATE', 'HEAVY', 'BLOCKED')
                                        NOT NULL DEFAULT 'MODERATE',
    algorithm_used      ENUM('DIJKSTRA', 'BFS', 'DFS', 'ASTAR')
                                        NOT NULL DEFAULT 'DIJKSTRA',
    dispatched_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_run_hospital (hospital_id),
    INDEX idx_run_accident (accident_node_id),
    INDEX idx_run_dispatched (dispatched_at),
    CONSTRAINT fk_run_hospital FOREIGN KEY (hospital_id) REFERENCES nodes(id) ON DELETE CASCADE,
    CONSTRAINT fk_run_accident FOREIGN KEY (accident_node_id) REFERENCES nodes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- View: vw_recent_runs
-- Shows recent ambulance dispatches with human-readable names
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW vw_recent_runs AS
SELECT
    r.id,
    h.name AS hospital_name,
    a.name AS accident_location,
    r.total_distance_km,
    r.eta_minutes,
    r.avg_speed_kmh,
    r.traffic_condition,
    r.algorithm_used,
    r.dispatched_at
FROM ambulance_runs r
JOIN nodes h ON r.hospital_id = h.id
JOIN nodes a ON r.accident_node_id = a.id
ORDER BY r.dispatched_at DESC;
