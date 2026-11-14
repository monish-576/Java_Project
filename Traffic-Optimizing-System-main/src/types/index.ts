export interface GraphNode {
  id: string;
  name: string;
  x: number;
  y: number;
}

export interface GraphEdge {
  id: string;
  source: string;
  target: string;
  distance: number;
  traffic: TrafficLevel;
}

export type TrafficLevel = 'normal' | 'moderate' | 'heavy' | 'blocked';

export interface GraphData {
  nodes: GraphNode[];
  edges: GraphEdge[];
}

export interface AlgorithmResult {
  path: string[];
  distance: number;
  visitedNodes: string[];
  executionTime: number;
  algorithm?: string;
  estimatedTravelTime?: number;
  pathFound?: boolean;
}

export type AlgorithmType = 'dijkstra' | 'bfs' | 'dfs' | 'astar' | 'bellmanford' | 'floydwarshall' | 'spfa' | 'bidijkstra' | 'johnson';

export interface RouteHistory {
  id: string;
  source: string;
  destination: string;
  sourceName: string;
  destinationName: string;
  algorithm: AlgorithmType;
  distance: number;
  path: string[];
  timestamp: number;
  trafficConditions: Record<string, TrafficLevel>;
}

export const TRAFFIC_MULTIPLIER: Record<TrafficLevel, number> = {
  normal: 1,
  moderate: 1.5,
  heavy: 2.5,
  blocked: Infinity,
};

export const TRAFFIC_COLORS: Record<TrafficLevel, string> = {
  normal: '#4ade80',
  moderate: '#facc15',
  heavy: '#fb7185',
  blocked: '#c084fc',
};

export const ALGORITHM_NAMES: Record<AlgorithmType, string> = {
  dijkstra: "Dijkstra's Algorithm",
  bfs: 'Breadth First Search (BFS)',
  dfs: 'Depth First Search (DFS)',
  astar: 'A* Search',
  bellmanford: 'Bellman-Ford',
  floydwarshall: 'Floyd-Warshall',
  spfa: 'SPFA (Shortest Path Faster Algorithm)',
  bidijkstra: 'Bidirectional Dijkstra',
  johnson: "Johnson's Algorithm",
};

export interface Vehicle {
  id: string;
  name: string;
  speed: number;
  icon: string;
  description: string;
}

export const DEFAULT_VEHICLES: Vehicle[] = [
  { id: 'ambulance', name: 'Ambulance', speed: 60, icon: 'ambulance', description: 'Emergency medical service vehicle' },
  { id: 'fire-truck', name: 'Fire Truck', speed: 50, icon: 'truck', description: 'Fire response vehicle' },
  { id: 'police', name: 'Police Car', speed: 70, icon: 'shield', description: 'Police patrol vehicle' },
  { id: 'helicopter', name: 'Helicopter', speed: 120, icon: 'helicopter', description: 'Air ambulance (traffic-free)' },
  { id: 'car', name: 'Car', speed: 40, icon: 'car', description: 'Standard passenger vehicle' },
  { id: 'bike', name: 'Motorcycle', speed: 35, icon: 'bike', description: 'Agile two-wheeler' },
  { id: 'truck', name: 'Truck', speed: 30, icon: 'truck', description: 'Heavy goods vehicle' },
];

export const TRAFFIC_SPEED_FACTOR: Record<TrafficLevel, number> = {
  normal: 1.0,
  moderate: 0.7,
  heavy: 0.4,
  blocked: 0,
};
