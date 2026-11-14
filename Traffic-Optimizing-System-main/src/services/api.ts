import axios from 'axios';
import { GraphNode, GraphEdge, AlgorithmResult, AlgorithmType } from '@/types';

const API_BASE = import.meta.env.VITE_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

function toAlgorithmResult(data: any, algorithm?: string): AlgorithmResult {
  const result: AlgorithmResult = {
    path: data.path ?? [],
    distance: data.distance ?? 0,
    visitedNodes: data.visitedNodes ?? [],
    executionTime: data.executionTimeMs ?? data.executionTime ?? 0,
    estimatedTravelTime: data.estimatedTravelTime ?? 0,
    algorithm: data.algorithm ?? algorithm,
    pathFound: data.pathFound ?? (data.path ? data.path.length > 0 : false),
  };
  return result;
}

// ============================================================
// Route / Algorithm API
// ============================================================

export async function findRoute(
  algorithm: AlgorithmType,
  source: string,
  destination: string,
  nodes: GraphNode[],
  edges: GraphEdge[]
): Promise<AlgorithmResult> {
  const payload = {
    source,
    destination,
    algorithm,
    nodes: nodes.map((n) => ({ id: n.id, name: n.name, x: n.x, y: n.y })),
    edges: edges.map((e) => ({
      id: e.id, source: e.source, target: e.target,
      distance: e.distance, traffic: e.traffic,
    })),
  };
  console.info(`[API] POST /routes/custom algo=${algorithm} src=${source} dst=${destination} nodes=${payload.nodes.length} edges=${payload.edges.length}`);
  const response = await api.post('/routes/custom', payload);
  console.info(`[API] Response status=${response.status} pathLength=${response.data?.path?.length}`);
  return toAlgorithmResult(response.data, algorithm);
}

export async function compareAlgorithms(
  source: string,
  destination: string,
  nodes: GraphNode[],
  edges: GraphEdge[]
): Promise<Record<AlgorithmType, AlgorithmResult>> {
  const algorithms: AlgorithmType[] = ['dijkstra', 'bfs', 'dfs', 'astar', 'bellmanford', 'floydwarshall', 'spfa', 'bidijkstra', 'johnson'];
  const results: Record<string, AlgorithmResult> = {};

  await Promise.all(
    algorithms.map(async (algo) => {
      results[algo] = await findRoute(algo, source, destination, nodes, edges);
    })
  );

  return results as Record<AlgorithmType, AlgorithmResult>;
}

export async function findBFS(
  source: string, destination: string, nodes: GraphNode[], edges: GraphEdge[]
): Promise<AlgorithmResult> {
  return findRoute('bfs', source, destination, nodes, edges);
}

export async function findDFS(
  source: string, destination: string, nodes: GraphNode[], edges: GraphEdge[]
): Promise<AlgorithmResult> {
  return findRoute('dfs', source, destination, nodes, edges);
}

export async function findDijkstra(
  source: string, destination: string, nodes: GraphNode[], edges: GraphEdge[]
): Promise<AlgorithmResult> {
  return findRoute('dijkstra', source, destination, nodes, edges);
}

export async function findAStar(
  source: string, destination: string, nodes: GraphNode[], edges: GraphEdge[]
): Promise<AlgorithmResult> {
  return findRoute('astar', source, destination, nodes, edges);
}

// ============================================================
// Graph Persistence API (Bulk)
// ============================================================

export async function saveGraphToServer(nodes: GraphNode[], edges: GraphEdge[]): Promise<boolean> {
  try {
    const payload = {
      nodes: nodes.map((n) => ({ id: n.id, name: n.name, x: n.x, y: n.y })),
      edges: edges.map((e) => ({
        id: e.id, source: e.source, target: e.target,
        distance: e.distance, traffic: e.traffic,
      })),
    };
    await api.post('/graph', payload);
    console.info(`[API] Graph saved to MySQL: ${nodes.length} nodes, ${edges.length} edges`);
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to save graph to MySQL:', err?.message || err);
    return false;
  }
}

export async function loadGraphFromServer(): Promise<{ nodes: GraphNode[]; edges: GraphEdge[] } | null> {
  try {
    const response = await api.get('/graph');
    const data = response.data;
    if (!data || !data.nodes) return null;
    console.info(`[API] Graph loaded from MySQL: ${data.nodes.length} nodes, ${data.edges?.length || 0} edges`);
    return {
      nodes: data.nodes.map((n: any) => ({ id: n.id, name: n.name, x: n.x, y: n.y })),
      edges: (data.edges || []).map((e: any) => ({
        id: e.id, source: e.source, target: e.target,
        distance: e.distance, traffic: e.traffic || 'normal',
      })),
    };
  } catch (err: any) {
    console.warn('[API] Failed to load graph from MySQL:', err?.message || err);
    return null;
  }
}

export async function clearGraphOnServer(): Promise<boolean> {
  try {
    await api.delete('/graph');
    console.info('[API] Graph cleared from MySQL');
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to clear graph from MySQL:', err?.message || err);
    return false;
  }
}

// ============================================================
// Individual Node CRUD API (Real-time sync)
// ============================================================

export async function addNodeToServer(node: GraphNode): Promise<boolean> {
  try {
    await api.post('/graph/nodes', {
      id: node.id,
      name: node.name,
      x: node.x,
      y: node.y,
    });
    console.info(`[API] Node added to MySQL: id=${node.id}, name=${node.name}`);
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to add node to MySQL:', err?.message || err);
    return false;
  }
}

export async function updateNodeOnServer(id: string, name: string, x: number, y: number): Promise<boolean> {
  try {
    await api.put(`/graph/nodes/${id}`, { name, x, y });
    console.info(`[API] Node updated in MySQL: id=${id}, name=${name}`);
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to update node in MySQL:', err?.message || err);
    return false;
  }
}

export async function deleteNodeOnServer(id: string): Promise<boolean> {
  try {
    await api.delete(`/graph/nodes/${id}`);
    console.info(`[API] Node deleted from MySQL: id=${id}`);
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to delete node from MySQL:', err?.message || err);
    return false;
  }
}

// ============================================================
// Individual Edge CRUD API (Real-time sync)
// ============================================================

export async function addEdgeToServer(edge: GraphEdge): Promise<boolean> {
  try {
    await api.post('/graph/edges', {
      id: edge.id,
      source: edge.source,
      target: edge.target,
      distance: edge.distance,
      traffic: edge.traffic,
    });
    console.info(`[API] Edge added to MySQL: id=${edge.id}, source=${edge.source}, target=${edge.target}`);
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to add edge to MySQL:', err?.message || err);
    return false;
  }
}

export async function updateEdgeOnServer(
  id: string, source: string, target: string, distance: number, traffic: string
): Promise<boolean> {
  try {
    await api.put(`/graph/edges/${id}`, { source, target, distance, traffic });
    console.info(`[API] Edge updated in MySQL: id=${id}`);
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to update edge in MySQL:', err?.message || err);
    return false;
  }
}

export async function deleteEdgeOnServer(id: string): Promise<boolean> {
  try {
    await api.delete(`/graph/edges/${id}`);
    console.info(`[API] Edge deleted from MySQL: id=${id}`);
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to delete edge from MySQL:', err?.message || err);
    return false;
  }
}

// ============================================================
// Route History API
// ============================================================

export interface RouteHistoryRecord {
  id: string;
  sourceNode: string;
  destinationNode: string;
  algorithm: string;
  distanceKm: number;
  pathJson: string;
  visitedCount: number;
  executionTimeMs: number;
  estimatedTravelTimeMin: number | null;
  createdAt: string;
}

export async function fetchRouteHistory(): Promise<RouteHistoryRecord[]> {
  try {
    const response = await api.get('/route-history');
    console.info(`[API] Fetched ${response.data.length} route history records`);
    return response.data;
  } catch (err: any) {
    console.warn('[API] Failed to fetch route history:', err?.message || err);
    return [];
  }
}

export async function saveRouteHistoryToServer(record: {
  sourceNode: string;
  destinationNode: string;
  algorithm: string;
  distanceKm: number;
  pathJson: string;
  visitedCount: number;
  executionTimeMs: number;
  estimatedTravelTimeMin: number | null;
}): Promise<boolean> {
  try {
    const response = await api.post('/route-history', record);
    console.info(`[API] Route history saved to MySQL: id=${response.data?.id}`);
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to save route history:', err?.message || err);
    return false;
  }
}

export async function deleteRouteHistoryFromServer(id: string): Promise<boolean> {
  try {
    await api.delete(`/route-history/${id}`);
    console.info(`[API] Route history deleted from MySQL: id=${id}`);
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to delete route history:', err?.message || err);
    return false;
  }
}

export async function clearRouteHistoryOnServer(): Promise<boolean> {
  try {
    await api.delete('/route-history');
    console.info('[API] All route history cleared from MySQL');
    return true;
  } catch (err: any) {
    console.warn('[API] Failed to clear route history:', err?.message || err);
    return false;
  }
}
