# Smart Route Optimization System

A full-stack web application for building road networks and finding optimal routes using 9 graph algorithms (Dijkstra, BFS, DFS, A*, Bellman-Ford, Floyd-Warshall, SPFA, Bidirectional Dijkstra, Johnson's). Features interactive graph visualization, traffic simulation, real-time traffic conditions, algorithm comparison, analytics dashboards, and emergency vehicle routing.

---

## Features

- **Interactive Graph Builder** — Create nodes and edges visually using React Flow. Set traffic levels per road.
- **Route Optimization** — Find shortest paths with any of 9 algorithms. Animated path traversal.
- **Traffic Simulation** — Dynamic road weights (normal, moderate, heavy, blocked) with color-coded edges.
- **Algorithm Comparison** — Run all 9 algorithms side-by-side with distance, visited nodes, and execution time.
- **Analytics Dashboard** — Graph statistics, traffic distribution pie chart, algorithm usage bar chart, route history line chart.
- **Emergency Routing** — Hospital-to-patient dispatch. Runs all 9 algorithms, recommends the shortest. ETA with traffic factors.
- **Sample Datasets** — Pre-built road networks (Chandigarh, University, Ambulance Network with traffic).
- **Import/Export** — Save and load graphs as JSON. Sync to MySQL database.
- **Undo/Redo** — Full history support (Ctrl+Z / Ctrl+Shift+Z).
- **Dark Theme** — Modern, responsive UI with dark color scheme.

---

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Frontend Framework | React | 18.3 |
| Language | TypeScript | 5.5 |
| Build Tool | Vite | 5.4 |
| Styling | Tailwind CSS | 3.4 |
| State Management | Zustand | 4.5 |
| Graph Visualization | React Flow | 11.11 |
| Charts | Recharts | 2.12 |
| HTTP Client | Axios | 1.17 |
| Animations | Framer Motion | 11.3 |
| Icons | Lucide React | 0.427 |
| Backend Framework | Spring Boot | 3.2.5 |
| Backend Language | Java | 21 |
| Database Access | Spring JDBC (JdbcTemplate) | — |
| Database | MySQL | 8.0+ |
| Build Tool (Backend) | Maven | — |

---

## Algorithms

| # | Algorithm | Type | Time Complexity | Space Complexity | Key Feature |
|---|---|---|---|---|---|
| 1 | Dijkstra | Weighted (SSSP) | O((V+E) log V) | O(V) | Standard shortest path with priority queue |
| 2 | BFS | Unweighted (SSSP) | O(V+E) | O(V) | Minimum number of hops |
| 3 | DFS | Depth-first | O(V+E) | O(V) | Path existence, explores deep first |
| 4 | A* | Heuristic (SSSP) | O(E) avg | O(V) | Fast with Euclidean distance heuristic |
| 5 | Bellman-Ford | Weighted (SSSP) | O(V·E) | O(V) | Handles negative edge weights, detects negative cycles |
| 6 | Floyd-Warshall | All-pairs | O(V³) | O(V²) | Computes shortest paths between all pairs |
| 7 | SPFA | Queue-based (SSSP) | O(E) avg | O(V) | Queue optimization of Bellman-Ford, faster on sparse graphs |
| 8 | Bidirectional Dijkstra | Bidirectional (SSSP) | O((V+E) log V) | O(V) | Searches from both source and destination simultaneously |
| 9 | Johnson | Reweighting (SSSP) | O(V·E + E log V) | O(V+E) | Handles negative weights via potential reweighting |

All algorithms account for **traffic multipliers** on edges:
- Normal: ×1.0 (green)
- Moderate: ×1.5 (yellow)
- Heavy: ×2.5 (red)
- Blocked: ∞ (gray, impassable)

---

## Architecture

```
┌──────────────────────────────────────────────────────┐
│                Frontend (React + Vite)                │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ │
│  │Dsb │ │Grph│ │Rte │ │Cmp │ │Anl │ │Emg │ │Stg │ │
│  │    │ │Bldr│ │Opt │ │    │ │ytcs│ │Rte │ │    │ │
│  └────┘ └────┘ └────┘ └────┘ └────┘ └────┘ └────┘ │
│  ┌──────────────────────────────────────────────────┐│
│  │         Zustand Store (graph, history, undo)      ││
│  └──────────────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────────────┐│
│  │   Client-side Algorithms (fallback) / Axios API   ││
│  └──────────────────────────────────────────────────┘│
│                     │ /api/* (proxy)                  │
└──────────────────────────────────────────────────────┘
                         │
┌──────────────────────────────────────────────────────┐
│               Backend (Spring Boot)                   │
│  ┌──────────────┐  ┌────────────┐  ┌──────────────┐ │
│  │  Controllers  │→ │  Services   │→ │ Repositories  │ │
│  │  (REST APIs)  │  │(Algorithm, │  │ (JDBC/MySQL) │ │
│  │               │  │ CRUD, etc) │  │              │ │
│  └──────────────┘  └────────────┘  └──────────────┘ │
│  ┌──────────────────────────────────────────────────┐│
│  │             9 Algorithm Classes                   ││
│  │  Dijkstra, BFS, DFS, A*, Bellman-Ford,           ││
│  │  Floyd-Warshall, SPFA, Bidirectional Dijkstra,   ││
│  │  Johnson                                         ││
│  └──────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │   MySQL Database    │
              │ (ambulance_routing) │
              └─────────────────────┘
```

---

## Project Structure

```
Smart-Route-Optimization-System/
├── backend/
│   ├── sql/
│   │   ├── schema.sql              # Full MySQL schema
│   │   ├── seed.sql                # Sample data
│   │   └── queries.sql             # Useful SQL queries
│   ├── src/main/java/com/ambulance/
│   │   ├── algorithms/             # 9 algorithm implementations
│   │   │   ├── Dijkstra.java
│   │   │   ├── BFS.java
│   │   │   ├── DFS.java
│   │   │   ├── AStar.java
│   │   │   ├── BellmanFord.java
│   │   │   ├── FloydWarshall.java
│   │   │   ├── SPFA.java
│   │   │   ├── BidirectionalDijkstra.java
│   │   │   ├── Johnson.java
│   │   │   └── Graph.java          # Graph utility class
│   │   ├── controller/             # REST API controllers
│   │   ├── service/                # Business logic services
│   │   ├── repository/             # JDBC repositories
│   │   ├── model/                  # Entities & DTOs
│   │   └── dto/                    # Request/Response DTOs
│   ├── pom.xml
│   └── application.properties
├── src/
│   ├── components/layout/          # Sidebar, Layout
│   ├── lib/
│   │   ├── algorithms.ts           # Client-side implementations (all 9)
│   │   ├── sampleData.ts           # Sample datasets
│   │   └── store.ts                # Zustand store
│   ├── pages/
│   │   ├── Dashboard.tsx
│   │   ├── GraphBuilder.tsx
│   │   ├── RouteOptimizer.tsx
│   │   ├── AlgorithmComparison.tsx
│   │   ├── Analytics.tsx
│   │   ├── EmergencyRouting.tsx
│   │   └── Settings.tsx
│   ├── services/
│   │   └── api.ts                  # Axios HTTP client
│   └── types/
│       └── index.ts                # TypeScript type definitions
├── package.json
├── vite.config.ts
└── README.md
```

---

## Database Schema

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

## REST API Endpoints

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
| `/api/route-history` | Route history CRUD |
| `/api/traffic` | Traffic conditions CRUD (by road, active congestion) |
| `/api/accidents` | Accident locations CRUD (active accidents) |

---

## Getting Started

### Prerequisites
- Node.js 18+
- Java 21+
- Maven 3.8+
- MySQL 8.0+

### Database Setup
```sql
mysql -u root -p < backend/sql/schema.sql
mysql -u root -p < backend/sql/seed.sql
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
Frontend starts on `http://localhost:5173` (proxies `/api` → `localhost:8080`)

### Production Build
```bash
npm run build     # outputs to dist/
npm run preview   # preview production build
```

---

## How to Use

### 1. Dashboard
The home page shows an overview — total nodes, roads, 9 algorithms available, and routes found. Quick-access cards link to all features. Recent route history is displayed at the bottom.

### 2. Graph Builder
Build your road network visually:
- **Add a Node** — Type a location name and click "Add Node". The node appears on the canvas.
- **Add an Edge** — Select source and destination, enter distance in km, choose traffic level (normal/moderate/heavy/blocked), and click "Add Edge". Or click-and-drag between two node handles on the canvas.
- **Traffic Control** — Change traffic level inline in the edge list. Color-coded indicators (green/yellow/red/purple).
- **Delete** — Click the trash icon next to any node or edge in the side panel.
- **Move Nodes** — Drag any node to reposition it on the canvas.
- **Undo/Redo** — `Ctrl+Z` / `Ctrl+Shift+Z` or use the toolbar buttons.
- **Sample Data** — Load pre-built datasets (Chandigarh Road Network, University Campus, Ambulance Network).
- **Import/Export** — Download graph as JSON or upload a previously saved graph.

### 3. Route Optimizer
- Select a **Source** and **Destination** from the dropdowns.
- Choose any of the **9 algorithms**.
- Click **"Find Route"** — the system animates the traversal, highlighting visited nodes in purple and the shortest path in green.
- The result panel shows total distance, visited nodes count, execution time, and the complete path.

### 4. Algorithm Comparison
- Select a source and destination.
- Click **"Compare All 9 Algorithms"**.
- A table shows each algorithm's distance, visited nodes, execution time, and whether a path was found.
- A bar chart visualizes the differences.
- Detailed paths for each algorithm are displayed below.

### 5. Analytics Dashboard
- **Key Metrics** — Total nodes, roads, average path length, most connected node.
- **Traffic Distribution** — Pie chart of roads at each traffic level.
- **Algorithm Usage** — Bar chart of algorithm usage frequency.
- **Route History** — Line chart of route distances over time with a full history table.

### 6. Emergency Routing
- Click **"Load Ambulance Network"** to load the emergency sample dataset (includes pre-set traffic).
- Select a **Hospital** and a **Patient Location**.
- Select a **Vehicle** (Ambulance, Fire Truck, Police, Helicopter, Car, Bike, Truck).
- Click **"Dispatch Emergency"** — the system runs all 9 algorithms, selects the shortest path, and calculates ETA considering worst-case traffic on the route.
- Results show estimated arrival time, distance, traffic level breakdown, average speed, and the full path.
- A comparison table shows all 9 algorithms' results.

### 7. Settings
- **Export** — Copy graph JSON to clipboard or download as a file.
- **Import** — Paste JSON to restore a previously saved graph.
- **Reset** — Clear all graph data and route history.
- **Data Summary** — See node/edge counts and storage size.

---

## Sample SQL Queries

```sql
-- Route history with location names
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

---

## License

MIT
