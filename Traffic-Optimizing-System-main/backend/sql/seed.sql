-- ============================================================
-- Smart Route Optimization System - Seed Data
-- Database: ambulance_routing
-- Uses correct table names: graph_nodes, graph_edges, route_history
-- ============================================================

USE ambulance_routing;

-- ------------------------------------------------------------
-- Graph Nodes
-- ------------------------------------------------------------
INSERT INTO graph_nodes (id, name, x, y) VALUES
('node-1', 'City Hospital', 10, 10),
('node-2', 'Accident Site Alpha', 50, 60),
('node-3', 'Ambulance Depot North', 5, 80),
('node-4', 'Junction A', 25, 30),
('node-5', 'Junction B', 40, 20),
('node-6', 'Junction C', 30, 50),
('node-7', 'Junction D', 60, 40),
('node-8', 'Eastside Community Center', 75, 25),
('node-9', 'South Park Junction', 55, 75),
('node-10', 'West Industrial Zone', 8, 15);

-- ------------------------------------------------------------
-- Graph Edges
-- ------------------------------------------------------------
INSERT INTO graph_edges (id, source, target, distance, traffic) VALUES
('edge-1',  'node-1', 'node-4', 25.0, 'normal'),
('edge-2',  'node-1', 'node-5', 32.0, 'moderate'),
('edge-3',  'node-4', 'node-5', 18.0, 'normal'),
('edge-4',  'node-4', 'node-6', 22.0, 'normal'),
('edge-5',  'node-5', 'node-7', 28.0, 'heavy'),
('edge-6',  'node-6', 'node-2', 20.0, 'normal'),
('edge-7',  'node-6', 'node-7', 15.0, 'normal'),
('edge-8',  'node-7', 'node-2', 18.0, 'moderate'),
('edge-9',  'node-3', 'node-6', 35.0, 'normal'),
('edge-10', 'node-3', 'node-4', 50.0, 'blocked'),
('edge-11', 'node-7', 'node-8', 12.0, 'normal'),
('edge-12', 'node-2', 'node-9', 16.0, 'moderate'),
('edge-13', 'node-9', 'node-8', 22.0, 'normal'),
('edge-14', 'node-1', 'node-10', 10.0, 'normal'),
('edge-15', 'node-10', 'node-4', 20.0, 'moderate');

-- ------------------------------------------------------------
-- Route History (algorithm execution records)
-- ------------------------------------------------------------
INSERT INTO route_history (id, source_node, destination_node, algorithm, distance_km, path_json, visited_count, execution_time_ms, traffic_conditions, estimated_travel_time_min) VALUES
('route-001', 'node-1', 'node-2', 'DIJKSTRA', 57.0, '["node-1","node-4","node-6","node-2"]', 5, 12, '{"node-1->node-4":"NORMAL","node-4->node-6":"NORMAL","node-6->node-2":"NORMAL"}', 45.0),
('route-002', 'node-3', 'node-2', 'ASTAR', 55.0, '["node-3","node-6","node-2"]', 4, 8, '{"node-3->node-6":"NORMAL","node-6->node-2":"NORMAL"}', 42.0),
('route-003', 'node-1', 'node-7', 'BFS', 60.0, '["node-1","node-4","node-5","node-7"]', 7, 15, NULL, 55.0),
('route-004', 'node-5', 'node-2', 'DIJKSTRA', 46.0, '["node-5","node-7","node-2"]', 3, 5, '{"node-5->node-7":"HEAVY","node-7->node-2":"MODERATE"}', 52.0),
('route-005', 'node-4', 'node-2', 'DFS', 57.0, '["node-4","node-1","node-5","node-7","node-2"]', 8, 22, NULL, 68.0),
('route-006', 'node-3', 'node-7', 'DIJKSTRA', 50.0, '["node-3","node-6","node-7"]', 4, 18, '{"node-3->node-6":"NORMAL","node-6->node-7":"NORMAL"}', 38.0);

-- ------------------------------------------------------------
-- Hospitals
-- ------------------------------------------------------------
INSERT INTO hospitals (id, name, address, latitude, longitude, phone, emergency_level) VALUES
('hosp-001', 'City General Hospital', '123 Main St, Downtown', 10.0, 10.0, '555-0100', 'trauma'),
('hosp-002', 'County Medical Center', '456 Oak Ave, Midtown', 22.5, 35.8, '555-0200', 'general'),
('hosp-003', 'University Hospital', '789 College Blvd, University District', 45.2, 28.3, '555-0300', 'trauma'),
('hosp-004', 'St. Mary''s Emergency Care', '321 Pine Rd, Suburb East', 70.1, 55.4, '555-0400', 'critical'),
('hosp-005', 'Valley Health Clinic', '159 River Dr, Westside', 15.3, 75.6, '555-0500', 'general');

-- ------------------------------------------------------------
-- Ambulances
-- ------------------------------------------------------------
INSERT INTO ambulances (id, hospital_id, vehicle_number, driver_name, driver_phone, status, current_lat, current_lng) VALUES
('amb-001', 'hosp-001', 'AMB-1001', 'John Smith', '555-1001', 'AVAILABLE', 10.5, 10.8),
('amb-002', 'hosp-001', 'AMB-1002', 'Sarah Johnson', '555-1002', 'AVAILABLE', 9.2, 11.5),
('amb-003', 'hosp-001', 'AMB-1003', 'Mike Brown', '555-1003', 'DISPATCHED', 12.0, 14.2),
('amb-004', 'hosp-002', 'AMB-2001', 'Emily Davis', '555-2001', 'AVAILABLE', 23.0, 36.0),
('amb-005', 'hosp-002', 'AMB-2002', 'Robert Wilson', '555-2002', 'TRANSPORTING', 24.5, 33.1),
('amb-006', 'hosp-003', 'AMB-3001', 'Lisa Martinez', '555-3001', 'AVAILABLE', 45.8, 28.0),
('amb-007', 'hosp-003', 'AMB-3002', 'James Taylor', '555-3002', 'ON_SCENE', 44.0, 30.5),
('amb-008', 'hosp-004', 'AMB-4001', 'Amanda Clark', '555-4001', 'AVAILABLE', 70.5, 55.0),
('amb-009', 'hosp-004', 'AMB-4002', 'David Lee', '555-4002', 'OFFLINE', NULL, NULL),
('amb-010', 'hosp-005', 'AMB-5001', 'Jessica White', '555-5001', 'AVAILABLE', 15.0, 76.0);

-- ------------------------------------------------------------
-- Traffic Conditions
-- ------------------------------------------------------------
INSERT INTO traffic_conditions (id, road_id, congestion_level, avg_speed_kmh, recorded_at, source) VALUES
('traffic-001', 'road-main-01', 'HEAVY', 15.0, NOW(), 'sensor'),
('traffic-002', 'road-main-02', 'MODERATE', 35.0, NOW(), 'sensor'),
('traffic-003', 'road-main-03', 'NORMAL', 55.0, NOW(), 'system'),
('traffic-004', 'road-main-04', 'BLOCKED', 0.0, NOW(), 'incident_report'),
('traffic-005', 'road-main-05', 'NORMAL', 60.0, NOW(), 'system'),
('traffic-006', 'road-side-01', 'MODERATE', 30.0, NOW(), 'sensor'),
('traffic-007', 'road-side-02', 'NORMAL', 45.0, NOW(), 'system'),
('traffic-008', 'road-hwy-01', 'HEAVY', 20.0, NOW(), 'traffic_api'),
('traffic-009', 'road-hwy-02', 'NORMAL', 80.0, NOW(), 'traffic_api'),
('traffic-010', 'road-conn-01', 'MODERATE', 28.0, NOW(), 'sensor');

-- ------------------------------------------------------------
-- Accident Locations
-- ------------------------------------------------------------
INSERT INTO accident_locations (id, node_id, latitude, longitude, severity, description, reported_at, cleared_at, is_active) VALUES
('acc-001', 'node-1', 50.0, 60.0, 'CRITICAL', 'Multi-vehicle collision on Highway 101 near Junction C', NOW() - INTERVAL 15 MINUTE, NULL, TRUE),
('acc-002', 'node-2', 35.0, 45.0, 'MODERATE', 'Rear-end collision at Oak & Pine intersection', NOW() - INTERVAL 2 HOUR, NULL, TRUE),
('acc-003', 'node-3', 20.0, 18.0, 'MINOR', 'Side-swipe in parking lot', NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 1 HOUR, FALSE),
('acc-004', 'node-4', 65.0, 30.0, 'SEVERE', 'Truck rollover on interstate ramp', NOW() - INTERVAL 30 MINUTE, NULL, TRUE),
('acc-005', 'node-5', 80.0, 70.0, 'MODERATE', 'Pedestrian incident at crosswalk', NOW() - INTERVAL 3 HOUR, NULL, TRUE);



SELECT * FROM graph_nodes;
SELECT * FROM graph_edges;

USE ambulance_routing;

-- Check graph data
SELECT COUNT(*) AS nodes_count FROM graph_nodes;
SELECT COUNT(*) AS edges_count FROM graph_edges;
SELECT * FROM graph_nodes;
SELECT * FROM graph_edges;





