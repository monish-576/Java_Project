-- ============================================================
-- Smart Route Optimization System - Useful SQL Queries
-- Database: ambulance_routing
-- ============================================================

USE ambulance_routing;

-- 1. All route history with source/destination names
SELECT
    rh.id,
    src.name AS source,
    dst.name AS destination,
    rh.algorithm,
    rh.distance_km,
    rh.visited_count,
    rh.execution_time_ms,
    rh.estimated_travel_time_min,
    rh.created_at
FROM route_history rh
LEFT JOIN graph_nodes src ON rh.source_node = src.id
LEFT JOIN graph_nodes dst ON rh.destination_node = dst.id
ORDER BY rh.created_at DESC;

-- 2. Route count per algorithm (which algorithm is used most)
SELECT
    algorithm,
    COUNT(*) AS times_used,
    ROUND(AVG(distance_km), 2) AS avg_distance_km,
    ROUND(AVG(execution_time_ms), 1) AS avg_time_ms
FROM route_history
GROUP BY algorithm
ORDER BY times_used DESC;

-- 3. Shortest and longest routes found
SELECT
    'Shortest' AS type,
    src.name AS source,
    dst.name AS destination,
    algorithm,
    distance_km,
    execution_time_ms
FROM route_history rh
JOIN graph_nodes src ON rh.source_node = src.id
JOIN graph_nodes dst ON rh.destination_node = dst.id
ORDER BY distance_km ASC
LIMIT 1;

SELECT
    'Longest' AS type,
    src.name AS source,
    dst.name AS destination,
    algorithm,
    distance_km,
    execution_time_ms
FROM route_history rh
JOIN graph_nodes src ON rh.source_node = src.id
JOIN graph_nodes dst ON rh.destination_node = dst.id
ORDER BY distance_km DESC
LIMIT 1;

-- 4. Fastest execution time by algorithm
SELECT
    algorithm,
    MIN(execution_time_ms) AS fastest_ms,
    MAX(execution_time_ms) AS slowest_ms,
    ROUND(AVG(execution_time_ms), 1) AS avg_ms
FROM route_history
GROUP BY algorithm
ORDER BY avg_ms;

-- 5. Routes with heaviest traffic conditions
SELECT
    src.name AS source,
    dst.name AS destination,
    rh.algorithm,
    rh.distance_km,
    rh.traffic_conditions,
    rh.estimated_travel_time_min
FROM route_history rh
JOIN graph_nodes src ON rh.source_node = src.id
JOIN graph_nodes dst ON rh.destination_node = dst.id
WHERE rh.traffic_conditions IS NOT NULL
  AND rh.traffic_conditions LIKE '%HEAVY%'
ORDER BY rh.estimated_travel_time_min DESC;

-- 6. All nodes and their connections (edges)
SELECT
    src.name AS source_node,
    dst.name AS destination_node,
    e.distance,
    e.traffic
FROM graph_edges e
JOIN graph_nodes src ON e.source = src.id
JOIN graph_nodes dst ON e.target = dst.id
ORDER BY src.name, dst.name;

-- 7. Node with most connections (degree count)
SELECT
    n.name,
    COUNT(*) AS connection_count
FROM (
    SELECT source AS node_id FROM graph_edges
    UNION ALL
    SELECT target FROM graph_edges
) connections
JOIN graph_nodes n ON connections.node_id = n.id
GROUP BY n.name
ORDER BY connection_count DESC;

-- 8. Active accident locations with node details
SELECT
    a.id,
    n.name AS location,
    a.severity,
    a.description,
    a.reported_at
FROM accident_locations a
JOIN graph_nodes n ON a.node_id = n.id
WHERE a.is_active = TRUE
ORDER BY a.reported_at DESC;

-- 9. Available ambulances with hospital info
SELECT
    amb.vehicle_number,
    amb.driver_name,
    amb.driver_phone,
    amb.status,
    h.name AS hospital_name
FROM ambulances amb
JOIN hospitals h ON amb.hospital_id = h.id
WHERE amb.status = 'AVAILABLE'
ORDER BY h.name;

-- 10. Summary statistics
SELECT
    (SELECT COUNT(*) FROM graph_nodes) AS total_nodes,
    (SELECT COUNT(*) FROM graph_edges) AS total_edges,
    (SELECT COUNT(*) FROM route_history) AS total_routes,
    (SELECT COUNT(DISTINCT algorithm) FROM route_history) AS algorithms_used,
    (SELECT ROUND(AVG(distance_km), 2) FROM route_history) AS avg_route_distance_km,
    (SELECT COUNT(*) FROM accident_locations WHERE is_active = TRUE) AS active_accidents;
