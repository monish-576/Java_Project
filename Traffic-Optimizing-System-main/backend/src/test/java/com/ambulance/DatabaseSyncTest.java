package com.ambulance;

import com.ambulance.dto.*;
import com.ambulance.model.*;
import com.ambulance.repository.*;
import com.ambulance.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying that every frontend action is persisted in MySQL.
 * Run with: mvn test -Dtest=DatabaseSyncTest
 * Requires a running MySQL instance with ambulance_routing database.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseSyncTest {

    @Autowired private GraphPersistenceService graphPersistenceService;
    @Autowired private RouteHistoryRepository routeHistoryRepository;
    @Autowired private GraphPersistenceRepository graphPersistenceRepository;
    @Autowired private AlgorithmService algorithmService;
    @Autowired private JdbcTemplate jdbc;

    private static final String TEST_NODE_ID = "test-node-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TEST_EDGE_ID = "test-edge-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TEST_NODE2_ID = "test-node2-" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    void cleanTestData() {
        jdbc.update("DELETE FROM graph_edges WHERE id LIKE 'test-%'");
        jdbc.update("DELETE FROM graph_nodes WHERE id LIKE 'test-%'");
        jdbc.update("DELETE FROM route_history WHERE id LIKE 'test-%'");
    }

    // ============================================================
    // Test 1: Add graph -> verify database record
    // ============================================================
    @Test
    @Order(1)
    void testAddGraph_PersistsToDatabase() {
        System.out.println("=== Test 1: Add graph -> verify database record ===");

        GraphNodeEntity node = new GraphNodeEntity(TEST_NODE_ID, "Test Node 1", 100.0, 200.0);
        GraphEdgeEntity edge = new GraphEdgeEntity(TEST_EDGE_ID, TEST_NODE_ID, TEST_NODE_ID, 10.0, "normal");

        // Save via service
        graphPersistenceRepository.saveNode(node);
        graphPersistenceRepository.saveEdge(edge);

        // Verify node in DB
        GraphNodeEntity savedNode = graphPersistenceRepository.findNodeById(TEST_NODE_ID).orElse(null);
        assertNotNull(savedNode, "Node should exist in database");
        assertEquals("Test Node 1", savedNode.getName());
        assertEquals(100.0, savedNode.getX());
        assertEquals(200.0, savedNode.getY());
        System.out.println("  Node verified in MySQL: id=" + savedNode.getId() + ", name=" + savedNode.getName());

        // Verify edge in DB
        GraphEdgeEntity savedEdge = graphPersistenceRepository.findEdgeById(TEST_EDGE_ID).orElse(null);
        assertNotNull(savedEdge, "Edge should exist in database");
        assertEquals(10.0, savedEdge.getDistance());
        assertEquals("normal", savedEdge.getTraffic());
        System.out.println("  Edge verified in MySQL: id=" + savedEdge.getId() + ", distance=" + savedEdge.getDistance());

        System.out.println("  PASSED: Graph creation persisted to MySQL");
    }

    // ============================================================
    // Test 2: Add node -> verify database record
    // ============================================================
    @Test
    @Order(2)
    void testAddNode_PersistsToDatabase() {
        System.out.println("=== Test 2: Add node -> verify database record ===");

        GraphNodeEntity node = new GraphNodeEntity(TEST_NODE_ID, "New Test Node", 150.0, 250.0);
        GraphNodeEntity saved = graphPersistenceRepository.saveNode(node);

        // Verify via fresh query
        GraphNodeEntity fromDb = graphPersistenceRepository.findNodeById(TEST_NODE_ID).orElse(null);
        assertNotNull(fromDb);
        assertEquals("New Test Node", fromDb.getName());
        assertEquals(150.0, fromDb.getX());
        assertEquals(250.0, fromDb.getY());

        System.out.println("  PASSED: Node creation persisted to MySQL: " + fromDb.getId());
    }

    // ============================================================
    // Test 3: Add edge -> verify database record
    // ============================================================
    @Test
    @Order(3)
    void testAddEdge_PersistsToDatabase() {
        System.out.println("=== Test 3: Add edge -> verify database record ===");

        // Ensure nodes exist
        graphPersistenceRepository.saveNode(new GraphNodeEntity(TEST_NODE_ID, "Source", 0, 0));
        graphPersistenceRepository.saveNode(new GraphNodeEntity(TEST_NODE2_ID, "Target", 100, 100));

        GraphEdgeEntity edge = new GraphEdgeEntity(TEST_EDGE_ID, TEST_NODE_ID, TEST_NODE2_ID, 25.5, "moderate");
        GraphEdgeEntity saved = graphPersistenceRepository.saveEdge(edge);

        GraphEdgeEntity fromDb = graphPersistenceRepository.findEdgeById(TEST_EDGE_ID).orElse(null);
        assertNotNull(fromDb);
        assertEquals(TEST_NODE_ID, fromDb.getSource());
        assertEquals(TEST_NODE2_ID, fromDb.getTarget());
        assertEquals(25.5, fromDb.getDistance());
        assertEquals("moderate", fromDb.getTraffic());

        System.out.println("  PASSED: Edge creation persisted to MySQL: " + fromDb.getId());
    }

    // ============================================================
    // Test 4: Create route -> verify database record
    // ============================================================
    @Test
    @Order(4)
    void testCreateRoute_PersistsToDatabase() {
        System.out.println("=== Test 4: Create route (algorithm execution) -> verify database record ===");

        RouteHistory history = new RouteHistory(
                UUID.randomUUID().toString(),
                "src-node",
                "dst-node",
                "DIJKSTRA",
                100.0,
                "[\"src-node\",\"mid-node\",\"dst-node\"]",
                3,
                15L,
                45.0
        );
        RouteHistory saved = routeHistoryRepository.save(history);

        assertNotNull(saved.getId());
        RouteHistory fromDb = routeHistoryRepository.findById(saved.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals("DIJKSTRA", fromDb.getAlgorithm());
        assertEquals(100.0, fromDb.getDistanceKm());
        assertEquals(15L, fromDb.getExecutionTimeMs());

        System.out.println("  PASSED: Route history persisted to MySQL: " + fromDb.getId());
    }

    // ============================================================
    // Test 5: Modify route -> verify database update
    // ============================================================
    @Test
    @Order(5)
    void testModifyRoute_UpdatesDatabase() {
        System.out.println("=== Test 5: Modify route -> verify database update ===");

        RouteHistory history = new RouteHistory(
                UUID.randomUUID().toString(),
                "src-node",
                "dst-node",
                "BFS",
                50.0,
                "[\"src-node\",\"dst-node\"]",
                2,
                5L,
                30.0
        );
        RouteHistory saved = routeHistoryRepository.save(history);

        // Modify
        saved.setDistanceKm(75.0);
        saved.setAlgorithm("ASTAR");
        saved.setExecutionTimeMs(10L);
        routeHistoryRepository.save(saved);

        RouteHistory fromDb = routeHistoryRepository.findById(saved.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals(75.0, fromDb.getDistanceKm());
        assertEquals("ASTAR", fromDb.getAlgorithm());
        assertEquals(10L, fromDb.getExecutionTimeMs());

        System.out.println("  PASSED: Route update persisted to MySQL: distance=" + fromDb.getDistanceKm());
    }

    // ============================================================
    // Test 6: Delete route -> verify database deletion
    // ============================================================
    @Test
    @Order(6)
    void testDeleteRoute_RemovesFromDatabase() {
        System.out.println("=== Test 6: Delete route -> verify database deletion ===");

        RouteHistory history = new RouteHistory(
                UUID.randomUUID().toString(),
                "src",
                "dst",
                "DFS",
                30.0,
                "[\"src\",\"dst\"]",
                2,
                3L,
                20.0
        );
        RouteHistory saved = routeHistoryRepository.save(history);

        // Verify exists
        assertTrue(routeHistoryRepository.existsById(saved.getId()));

        // Delete
        routeHistoryRepository.deleteById(saved.getId());

        // Verify gone
        assertFalse(routeHistoryRepository.existsById(saved.getId()));

        System.out.println("  PASSED: Route deletion removed from MySQL");
    }

    // ============================================================
    // Test 7: Execute Dijkstra -> verify execution history
    // ============================================================
    @Test
    @Order(7)
    void testDijkstraExecution_SavesToDatabase() {
        System.out.println("=== Test 7: Execute Dijkstra -> verify execution history ===");

        GraphNodeEntity node1 = graphPersistenceRepository.saveNode(
                new GraphNodeEntity("dij-node-1", "A", 0, 0));
        GraphNodeEntity node2 = graphPersistenceRepository.saveNode(
                new GraphNodeEntity("dij-node-2", "B", 100, 0));
        GraphNodeEntity node3 = graphPersistenceRepository.saveNode(
                new GraphNodeEntity("dij-node-3", "C", 50, 50));

        graphPersistenceRepository.saveEdge(
                new GraphEdgeEntity("dij-edge-1", "dij-node-1", "dij-node-3", 10, "normal"));
        graphPersistenceRepository.saveEdge(
                new GraphEdgeEntity("dij-edge-2", "dij-node-3", "dij-node-2", 10, "normal"));

        // Execute algorithm via AlgorithmService
        RouteResponse result = algorithmService.findPath(
                "dijkstra",
                List.of(
                        new GraphNode("dij-node-1", "A", 0, 0),
                        new GraphNode("dij-node-2", "B", 100, 0),
                        new GraphNode("dij-node-3", "C", 50, 50)
                ),
                List.of(
                        new GraphEdge("dij-edge-1", "dij-node-1", "dij-node-3", 10, "normal"),
                        new GraphEdge("dij-edge-2", "dij-node-3", "dij-node-2", 10, "normal")
                ),
                "dij-node-1",
                "dij-node-2"
        );

        assertTrue(result.isPathFound(), "Dijkstra should find a path");
        assertEquals(20.0, result.getDistance(), 0.1);

        // Verify history was saved
        List<RouteHistory> allHistory = routeHistoryRepository.findAll();
        boolean found = allHistory.stream()
                .anyMatch(h -> "DIJKSTRA".equals(h.getAlgorithm())
                        && "dij-node-1".equals(h.getSourceNode())
                        && "dij-node-2".equals(h.getDestinationNode()));
        assertTrue(found, "Dijkstra execution should be saved in route history");

        System.out.println("  PASSED: Dijkstra execution saved to MySQL route_history");
    }

    // ============================================================
    // Test 8: Execute BFS -> verify execution history
    // ============================================================
    @Test
    @Order(8)
    void testBFSExecution_SavesToDatabase() {
        System.out.println("=== Test 8: Execute BFS -> verify execution history ===");

        RouteResponse result = algorithmService.findPath(
                "bfs",
                List.of(
                        new GraphNode("bfs-node-1", "Start", 0, 0),
                        new GraphNode("bfs-node-2", "End", 100, 0)
                ),
                List.of(
                        new GraphEdge("bfs-edge-1", "bfs-node-1", "bfs-node-2", 50, "normal")
                ),
                "bfs-node-1",
                "bfs-node-2"
        );

        assertTrue(result.isPathFound(), "BFS should find a path");
        assertNotNull(result.getAlgorithm());

        List<RouteHistory> allHistory = routeHistoryRepository.findAll();
        boolean found = allHistory.stream()
                .anyMatch(h -> "BFS".equals(h.getAlgorithm())
                        && "bfs-node-1".equals(h.getSourceNode()));
        assertTrue(found, "BFS execution should be saved in route history");

        System.out.println("  PASSED: BFS execution saved to MySQL route_history");
    }

    // ============================================================
    // Test 9: Execute DFS -> verify execution history
    // ============================================================
    @Test
    @Order(9)
    void testDFSExecution_SavesToDatabase() {
        System.out.println("=== Test 9: Execute DFS -> verify execution history ===");

        RouteResponse result = algorithmService.findPath(
                "dfs",
                List.of(
                        new GraphNode("dfs-node-1", "Start", 0, 0),
                        new GraphNode("dfs-node-2", "End", 100, 0)
                ),
                List.of(
                        new GraphEdge("dfs-edge-1", "dfs-node-1", "dfs-node-2", 50, "normal")
                ),
                "dfs-node-1",
                "dfs-node-2"
        );

        assertTrue(result.isPathFound(), "DFS should find a path");
        assertNotNull(result.getAlgorithm());

        List<RouteHistory> allHistory = routeHistoryRepository.findAll();
        boolean found = allHistory.stream()
                .anyMatch(h -> "DFS".equals(h.getAlgorithm())
                        && "dfs-node-1".equals(h.getSourceNode()));
        assertTrue(found, "DFS execution should be saved in route history");

        System.out.println("  PASSED: DFS execution saved to MySQL route_history");
    }

    // ============================================================
    // Test 10: Execute A* -> verify execution history
    // ============================================================
    @Test
    @Order(10)
    void testAStarExecution_SavesToDatabase() {
        System.out.println("=== Test 10: Execute A* -> verify execution history ===");

        RouteResponse result = algorithmService.findPath(
                "astar",
                List.of(
                        new GraphNode("astar-node-1", "Start", 0, 0),
                        new GraphNode("astar-node-2", "End", 100, 0)
                ),
                List.of(
                        new GraphEdge("astar-edge-1", "astar-node-1", "astar-node-2", 50, "normal")
                ),
                "astar-node-1",
                "astar-node-2"
        );

        assertTrue(result.isPathFound(), "A* should find a path");
        assertNotNull(result.getAlgorithm());

        List<RouteHistory> allHistory = routeHistoryRepository.findAll();
        boolean found = allHistory.stream()
                .anyMatch(h -> "ASTAR".equals(h.getAlgorithm())
                        && "astar-node-1".equals(h.getSourceNode()));
        assertTrue(found, "A* execution should be saved in route history");

        System.out.println("  PASSED: A* execution saved to MySQL route_history");
    }

    // ============================================================
    // Test 11: Update node -> verify database update
    // ============================================================
    @Test
    @Order(11)
    void testUpdateNode_UpdatesDatabase() {
        System.out.println("=== Test 11: Update node -> verify database update ===");

        GraphNodeEntity node = graphPersistenceRepository.saveNode(
                new GraphNodeEntity(TEST_NODE_ID, "Original Name", 10, 20));

        // Update via service
        graphPersistenceService.updateNode(TEST_NODE_ID, "Updated Name", 30, 40);

        GraphNodeEntity fromDb = graphPersistenceRepository.findNodeById(TEST_NODE_ID).orElse(null);
        assertNotNull(fromDb);
        assertEquals("Updated Name", fromDb.getName());
        assertEquals(30.0, fromDb.getX());
        assertEquals(40.0, fromDb.getY());

        System.out.println("  PASSED: Node update persisted to MySQL: " + fromDb.getName());
    }

    // ============================================================
    // Test 12: Delete node -> verify database deletion (cascades to edges)
    // ============================================================
    @Test
    @Order(12)
    void testDeleteNode_RemovesFromDatabase() {
        System.out.println("=== Test 12: Delete node -> verify database deletion ===");

        String delNodeId = "del-node-" + UUID.randomUUID().toString().substring(0, 8);
        graphPersistenceRepository.saveNode(new GraphNodeEntity(delNodeId, "To Delete", 0, 0));

        assertTrue(graphPersistenceRepository.nodeExists(delNodeId));

        graphPersistenceService.deleteNode(delNodeId);

        assertFalse(graphPersistenceRepository.nodeExists(delNodeId));
        System.out.println("  PASSED: Node deletion removed from MySQL");
    }

    // ============================================================
    // Test 13: Delete edge -> verify database deletion
    // ============================================================
    @Test
    @Order(13)
    void testDeleteEdge_RemovesFromDatabase() {
        System.out.println("=== Test 13: Delete edge -> verify database deletion ===");

        String delEdgeNode1 = "del-edge-n1-" + UUID.randomUUID().toString().substring(0, 4);
        String delEdgeNode2 = "del-edge-n2-" + UUID.randomUUID().toString().substring(0, 4);
        graphPersistenceRepository.saveNode(new GraphNodeEntity(delEdgeNode1, "E1", 0, 0));
        graphPersistenceRepository.saveNode(new GraphNodeEntity(delEdgeNode2, "E2", 100, 0));

        String delEdgeId = "del-edge-" + UUID.randomUUID().toString().substring(0, 8);
        graphPersistenceRepository.saveEdge(new GraphEdgeEntity(delEdgeId, delEdgeNode1, delEdgeNode2, 10, "normal"));

        assertTrue(graphPersistenceRepository.edgeExists(delEdgeId));

        graphPersistenceService.deleteEdge(delEdgeId);

        assertFalse(graphPersistenceRepository.edgeExists(delEdgeId));
        System.out.println("  PASSED: Edge deletion removed from MySQL");
    }

    // ============================================================
    // Test 14: Bulk graph save -> verify all records in database
    // ============================================================
    @Test
    @Order(14)
    void testBulkGraphSave_AllRecordsInDatabase() {
        System.out.println("=== Test 14: Bulk graph save -> verify all records in database ===");

        GraphSaveRequest request = new GraphSaveRequest();
        GraphSaveRequest.NodeDTO n1 = new GraphSaveRequest.NodeDTO();
        n1.setId("bulk-n1");
        n1.setName("Bulk Node 1");
        n1.setX(10);
        n1.setY(20);
        GraphSaveRequest.NodeDTO n2 = new GraphSaveRequest.NodeDTO();
        n2.setId("bulk-n2");
        n2.setName("Bulk Node 2");
        n2.setX(30);
        n2.setY(40);
        request.setNodes(List.of(n1, n2));

        GraphSaveRequest.EdgeDTO e1 = new GraphSaveRequest.EdgeDTO();
        e1.setId("bulk-e1");
        e1.setSource("bulk-n1");
        e1.setTarget("bulk-n2");
        e1.setDistance(15.0);
        e1.setTraffic("heavy");
        request.setEdges(List.of(e1));

        graphPersistenceService.save(request);

        // Verify in DB
        assertTrue(graphPersistenceRepository.nodeExists("bulk-n1"));
        assertTrue(graphPersistenceRepository.nodeExists("bulk-n2"));
        assertTrue(graphPersistenceRepository.edgeExists("bulk-e1"));

        GraphLoadResponse loaded = graphPersistenceService.load();
        assertEquals(2, loaded.getNodes().size());
        assertEquals(1, loaded.getEdges().size());

        System.out.println("  PASSED: Bulk graph save verified in MySQL");
    }

    // ============================================================
    // Test 15: Clear graph -> verify database is empty
    // ============================================================
    @Test
    @Order(15)
    void testClearGraph_DatabaseEmpty() {
        System.out.println("=== Test 15: Clear graph -> verify database is empty ===");

        // First save some data
        graphPersistenceRepository.saveNode(new GraphNodeEntity("clear-n1", "Clear Me", 0, 0));
        graphPersistenceRepository.saveNode(new GraphNodeEntity("clear-n2", "Clear Me Too", 100, 100));
        assertTrue(graphPersistenceRepository.findAllNodes().size() >= 2);

        // Clear
        graphPersistenceService.clear();

        // Verify empty
        assertEquals(0, graphPersistenceRepository.findAllNodes().size());
        assertEquals(0, graphPersistenceRepository.findAllEdges().size());

        System.out.println("  PASSED: Graph cleared from MySQL");
    }
}
