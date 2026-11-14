# Smart Route Optimization System — Project Report

## 1. Introduction

Smart Route Optimization System is a full-stack web application for building road networks, simulating traffic conditions, and finding optimal routes using 9 different graph algorithms. It features an interactive graph builder, real-time traffic simulation, algorithm comparison, analytics dashboard, and emergency vehicle routing.

---

## 2. Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Frontend Framework | React | 18.3 |
| Language | TypeScript | 5.5 |
| Build Tool | Vite | 5.4 |
| Styling | Tailwind CSS | 3.4 |
| State Management | Zustand | 4.5 |
| Graph Visualization | React Flow | 11.11 |
| Charts | Recharts | 2.12 |
| Backend Framework | Spring Boot | 3.2.5 |
| Backend Language | Java | 21 |
| Database | MySQL | 8.0+ |
| JDBC Driver | mysql-connector-j | — |
| HTTP Client (Frontend) | Axios | 1.17 |
| Animations | Framer Motion | 11.3 |

---

## 3. Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React + Vite)                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐  │
│  │Dashboard │  │  Graph   │  │  Route   │  │Emergency   │  │
│  │          │  │ Builder  │  │Optimizer │  │Routing     │  │
│  ├──────────┤  ├──────────┤  ├──────────┤  ├────────────┤  │
│  │Algorithm │  │Analytics │  │ Settings │  │Comparison  │  │
│  │Comparison│  │          │  │          │  │            │  │
│  └──────────┘  └──────────┘  └──────────┘  └────────────┘  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Zustand Store (State Management)         │   │
│  │  ┌──────────┐  ┌──────────┐  ┌────────────────────┐  │   │
│  │  │  Graph   │  │ History  │  │ Undo/Redo Stack    │  │   │
│  │  │ (nodes,  │  │ (route   │  │                    │  │   │
│  │  │  edges)  │  │  records)│  │                    │  │   │
│  │  └──────────┘  └──────────┘  └────────────────────┘  │   │
│  └──────────────────────────────────────────────────────┘   │
│                     │ Axios HTTP calls                       │
│                     ▼                                        │
│              /api/* (proxy → localhost:8080)                 │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   Backend (Spring Boot 3.2.5)                │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Controllers  │  │   Services   │  │  Repositories    │  │
│  │  (REST APIs)  │──▶│  (Business  │──▶│  (JDBC / MySQL)  │  │
│  │               │  │   Logic)     │  │                  │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
│                           │                                 │
│  ┌────────────────────────▼──────────────────────────────┐  │
│  │              Algorithm Engine                          │  │
│  │  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐  │  │
│  │  │ DJK│ │ BFS│ │ DFS│ │ A* │ │ BF │ │ FW │ │SPFA│  │  │
│  │  └────┘ └────┘ └────┘ └────┘ └────┘ └────┘ └────┘  │  │
│  │  ┌────┐ ┌────┐                                       │  │
│  │  │ BiD│ │Jhn │                                       │  │
│  │  └────┘ └────┘                                       │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
              ┌─────────────────────────────┐
              │      MySQL Database         │
              │   (ambulance_routing)       │
              │  ┌──────────────────────┐   │
              │  │ graph_nodes          │   │
              │  │ graph_edges          │   │
              │  │ route_history        │   │
              │  │ hospitals            │   │
              │  │ ambulances           │   │
              │  │ traffic_conditions   │   │
              │  │ accident_locations   │   │
              │  └──────────────────────┘   │
              └─────────────────────────────┘
```

The frontend communicates with the backend via REST APIs through a Vite proxy (`/api` → `localhost:8080`). The backend uses Spring Boot with JDBC (`JdbcTemplate`) to interact with MySQL. The Zustand store manages the global state on the frontend and syncs with the backend for persistence.

---

## 4. Frontend Pages

| Page | Route | Description |
|---|---|---|
| **Dashboard** | `/` | Overview with node/edge/algorithm counts, quick-access cards, recent route history, and a step-by-step user guide |
| **Graph Builder** | `/graph-builder` | Interactive canvas using React Flow to create/edit nodes and edges with traffic controls. Supports drag-drop, undo/redo, import/export JSON |
| **Route Optimizer** | `/route-optimizer` | Select source/destination, choose from 9 algorithms, find shortest path with animated visualization |
| **Algorithm Comparison** | `/comparison` | Run all 9 algorithms side-by-side. Table shows distance, visited nodes, execution time for each. Bar chart visualization |
| **Analytics** | `/analytics` | Metrics (total nodes, roads, avg path length), traffic distribution pie chart, algorithm usage bar chart, route history line chart |
| **Emergency Routing** | `/emergency` | Hospital-to-patient dispatching. Runs all 9 algorithms, recommends the shortest. Vehicle selection, ETA calculation with traffic factors |
| **Settings** | `/settings` | Import/export graph JSON, reset data, view data summary |

---

## 5. Backend REST API Endpoints

### Route Algorithms (`/api/routes`)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/routes/dijkstra` | Dijkstra's Shortest Path |
| GET | `/api/routes/bfs` | Breadth-First Search |
| GET | `/api/routes/dfs` | Depth-First Search |
| GET | `/api/routes/astar` | A* Search |
| GET | `/api/routes/bellmanford` | Bellman-Ford Algorithm |
| GET | `/api/routes/floydwarshall` | Floyd-Warshall Algorithm |
| GET | `/api/routes/spfa` | Shortest Path Faster Algorithm |
| GET | `/api/routes/bidijkstra` | Bidirectional Dijkstra |
| GET | `/api/routes/johnson` | Johnson's Algorithm |
| POST | `/api/routes/custom` | Run any algorithm (JSON body) |

### Graph CRUD (`/api/graph`)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/graph` | Bulk save graph |
| GET | `/api/graph` | Bulk load graph |
| DELETE | `/api/graph` | Clear all graph data |
| POST | `/api/graph/nodes` | Add single node |
| PUT | `/api/graph/nodes/{id}` | Update node |
| DELETE | `/api/graph/nodes/{id}` | Delete node |
| POST | `/api/graph/edges` | Add single edge |
| PUT | `/api/graph/edges/{id}` | Update edge (including traffic) |
| DELETE | `/api/graph/edges/{id}` | Delete edge |

### Other Endpoints

| Base Path | Description |
|---|---|
| `/api/hospitals` | Hospital CRUD |
| `/api/ambulances` | Ambulance CRUD (by hospital, by status) |
| `/api/roads` | Road network metadata CRUD |
| `/api/route-history` | Route execution history CRUD |
| `/api/traffic` | Traffic conditions CRUD (by road, active congestion) |
| `/api/accidents` | Accident locations CRUD (active accidents) |

---

## 6. Algorithms Implemented

| # | Algorithm | Type | Time Complexity | Space Complexity | Key Feature |
|---|---|---|---|---|---|
| 1 | Dijkstra | Weighted (SSSP) | O((V+E) log V) | O(V) | Standard shortest path with priority queue |
| 2 | BFS | Unweighted (SSSP) | O(V+E) | O(V) | Minimum number of hops |
| 3 | DFS | Depth-first | O(V+E) | O(V) | Path existence, explores deep first |
| 4 | A* | Heuristic (SSSP) | O(E) avg | O(V) | Fast with Euclidean distance heuristic |
| 5 | Bellman-Ford | Weighted (SSSP) | O(V·E) | O(V) | Handles **negative edge weights**, detects negative cycles |
| 6 | Floyd-Warshall | All-pairs | O(V³) | O(V²) | Computes shortest paths between **all pairs** |
| 7 | SPFA | Queue-based (SSSP) | O(E) avg | O(V) | Queue optimization of Bellman-Ford, faster on sparse graphs |
| 8 | Bidirectional Dijkstra | Bidirectional (SSSP) | O((V+E) log V) | O(V) | Searches from both source and destination simultaneously (~2x faster) |
| 9 | Johnson | Reweighting (SSSP) | O(V·E + E log V) | O(V+E) | Handles negative weights via potential reweighting |

**SSSP = Single Source Shortest Path**

All algorithms account for **traffic multipliers** on edges:
- Normal: ×1.0
- Moderate: ×1.5
- Heavy: ×2.5
- Blocked: ∞ (impassable)

---

## 7. Database Schema (MySQL — `ambulance_routing`)

| Table | Description | Key Columns |
|---|---|---|
| `graph_nodes` | Graph vertices | `id`, `name`, `x`, `y` |
| `graph_edges` | Road connections with traffic | `id`, `source`, `target`, `distance`, `traffic` |
| `route_history` | Algorithm execution records | `id`, `source_node`, `destination_node`, `algorithm`, `distance_km`, `path_json`, `execution_time_ms`, `traffic_conditions` |
| `hospitals` | Medical facilities | `id`, `name`, `address`, `latitude`, `longitude`, `emergency_level` |
| `ambulances` | Emergency vehicles | `id`, `hospital_id`, `vehicle_number`, `driver_name`, `status`, `current_lat`, `current_lng` |
| `traffic_conditions` | Real-time traffic data | `road_id`, `congestion_level`, `avg_speed_kmh`, `source` |
| `accident_locations` | Incident reports | `node_id`, `latitude`, `longitude`, `severity`, `is_active` |

---

## 8. Traffic Simulation System

Each edge (road) has a traffic level that affects route calculation:

| Traffic Level | Color | Distance Multiplier | Speed Factor | Effect on Route |
|---|---|---|---|---|
| Normal | Green | ×1.0 | 1.0 | No impact |
| Moderate | Yellow | ×1.5 | 0.7 | Road mildly congested |
| Heavy | Red | ×2.5 | 0.4 | Road heavily congested, algorithms avoid if possible |
| Blocked | Purple | ∞ | 0.0 | Road impassable, excluded entirely |

**How to set traffic:**
- **Graph Builder** — Select traffic level in the "Add Edge" panel, or change it inline in the edge list
- **Sample Data** — Pre-set traffic on ambulance network edges
- **API** — Update edge via `PUT /api/graph/edges/{id}` with the `traffic` field
- **Traffic Conditions API** — POST to `/api/traffic` for real-time condition records

---

## 9. Emergency Routing Feature

The emergency routing section:
1. Runs **all 9 algorithms** simultaneously on the hospital→patient path
2. Automatically selects the one that returns the **shortest distance**
3. Calculates ETA based on vehicle speed and worst traffic factor along the path
4. Shows a **comparison table** of all algorithms with distances and execution times
5. Recommends the fastest vehicle from the predefined list (Ambulance, Fire Truck, Police, Helicopter, Car, Bike, Truck)

---

## 10. How to Run

### Prerequisites
- Node.js 18+
- Java 21+
- Maven 3.8+
- MySQL 8.0+

### Database Setup
```sql
SOURCE backend/sql/schema.sql;
SOURCE backend/sql/seed.sql;
```

### Backend (Terminal 1)
```bash
cd backend
mvn spring-boot:run
```
Backend starts on `http://localhost:8080`

### Frontend (Terminal 2)
```bash
npm install
npm run dev
```
Frontend starts on `http://localhost:5173` (proxies `/api` to backend)

---

## 11. Sample SQL Queries

```sql
-- All route history with location names
SELECT rh.id, src.name AS source, dst.name AS destination,
       rh.algorithm, rh.distance_km, rh.execution_time_ms
FROM route_history rh
JOIN graph_nodes src ON rh.source_node = src.id
JOIN graph_nodes dst ON rh.destination_node = dst.id
ORDER BY rh.created_at DESC;

-- Algorithm usage statistics
SELECT algorithm, COUNT(*) AS times_used,
       ROUND(AVG(distance_km), 2) AS avg_distance
FROM route_history
GROUP BY algorithm
ORDER BY times_used DESC;

-- Active accidents with locations
SELECT a.id, n.name AS location, a.severity, a.description
FROM accident_locations a
JOIN graph_nodes n ON a.node_id = n.id
WHERE a.is_active = TRUE;
```
