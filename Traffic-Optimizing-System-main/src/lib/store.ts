import { create } from 'zustand';
import { v4 as uuidv4 } from 'uuid';
import { GraphNode, GraphEdge, TrafficLevel, RouteHistory, AlgorithmResult, AlgorithmType } from '@/types';
import {
  findRoute as apiFindRoute,
  compareAlgorithms as apiCompareAlgorithms,
  saveGraphToServer,
  loadGraphFromServer,
  addNodeToServer,
  updateNodeOnServer,
  deleteNodeOnServer,
  addEdgeToServer,
  updateEdgeOnServer,
  deleteEdgeOnServer,
  saveRouteHistoryToServer,
  fetchRouteHistory,
  deleteRouteHistoryFromServer,
  clearRouteHistoryOnServer,
  clearGraphOnServer,
} from '@/services/api';
import { runClientAlgorithm, clientAlgorithms as clientAlgos } from '@/lib/algorithms';

interface GraphStore {
  nodes: GraphNode[];
  edges: GraphEdge[];
  history: RouteHistory[];
  selectedAlgorithm: AlgorithmType;

  addNode: (name: string, x: number, y: number) => void;
  updateNode: (id: string, updates: Partial<GraphNode>) => void;
  removeNode: (id: string) => void;
  addEdge: (source: string, target: string, distance: number, traffic?: TrafficLevel) => void;
  updateEdge: (id: string, updates: Partial<GraphEdge>) => void;
  removeEdge: (id: string) => void;
  setTraffic: (edgeId: string, traffic: TrafficLevel) => void;
  setAlgorithm: (algo: AlgorithmType) => void;

  findRoute: (source: string, destination: string) => Promise<AlgorithmResult | null>;
  compareAlgorithms: (source: string, destination: string) => Promise<Record<AlgorithmType, AlgorithmResult>>;

  loadSampleData: (data: { nodes: GraphNode[]; edges: GraphEdge[] }) => void;
  clearGraph: () => void;
  exportGraph: () => string;
  importGraph: (json: string) => void;
  clearHistory: () => void;
  saveToServer: () => Promise<boolean>;
  loadFromServer: () => Promise<boolean>;
  syncStatus: 'idle' | 'saving' | 'saved' | 'error';
  syncMessage: string;

  // Undo/Redo
  undoStack: { nodes: GraphNode[]; edges: GraphEdge[] }[];
  redoStack: { nodes: GraphNode[]; edges: GraphEdge[] }[];
  pushState: () => void;
  undo: () => void;
  redo: () => void;
}

const loadFromStorage = () => {
  try {
    const data = localStorage.getItem('route-graph');
    if (data) return JSON.parse(data);
  } catch {}
  return { nodes: [], edges: [], history: [] };
};

const saveToStorage = (nodes: GraphNode[], edges: GraphEdge[], history: RouteHistory[]) => {
  localStorage.setItem('route-graph', JSON.stringify({ nodes, edges, history }));
};

const initial = loadFromStorage();

// Try loading from MySQL server on startup; fall back to localStorage
if (initial.nodes.length === 0) {
  loadGraphFromServer().then((data) => {
    if (data && data.nodes.length > 0) {
      useGraphStore.setState({ nodes: data.nodes, edges: data.edges, syncStatus: 'saved', syncMessage: 'Graph loaded from MySQL' });
      saveToStorage(data.nodes, data.edges, []);
      console.info(`[Store] Auto-loaded ${data.nodes.length} nodes from MySQL`);
    }
  });
}

// Load route history from MySQL on startup
fetchRouteHistory().then((records) => {
  if (records.length > 0) {
    const historyEntries: RouteHistory[] = records.map((r) => ({
      id: r.id,
      source: r.sourceNode,
      destination: r.destinationNode,
      sourceName: r.sourceNode,
      destinationName: r.destinationNode,
      algorithm: r.algorithm.toLowerCase() as AlgorithmType,
      distance: r.distanceKm,
      path: parsePathJson(r.pathJson),
      timestamp: new Date(r.createdAt).getTime(),
      trafficConditions: {},
    }));
    const existing = useGraphStore.getState().history;
    if (existing.length === 0) {
      useGraphStore.setState({ history: historyEntries });
      saveToStorage(useGraphStore.getState().nodes, useGraphStore.getState().edges, historyEntries);
      console.info(`[Store] Auto-loaded ${historyEntries.length} route history records from MySQL`);
    }
  }
});

function parsePathJson(json: string): string[] {
  try {
    return JSON.parse(json);
  } catch {
    return [];
  }
}

export const useGraphStore = create<GraphStore>((set, get) => ({
  nodes: initial.nodes ?? [],
  edges: initial.edges ?? [],
  history: initial.history ?? [],
  selectedAlgorithm: 'dijkstra',
  syncStatus: 'idle',
  syncMessage: '',
  undoStack: [],
  redoStack: [],

  pushState: () => {
    const { nodes, edges, undoStack } = get();
    set({ undoStack: [...undoStack.slice(-50), { nodes: [...nodes], edges: [...edges] }], redoStack: [] });
  },

  undo: () => {
    const { undoStack, nodes, edges } = get();
    if (undoStack.length === 0) return;
    const prev = undoStack[undoStack.length - 1];
    set({
      undoStack: undoStack.slice(0, -1),
      redoStack: [...get().redoStack, { nodes: [...nodes], edges: [...edges] }],
      nodes: prev.nodes,
      edges: prev.edges,
    });
  },

  redo: () => {
    const { redoStack, nodes, edges } = get();
    if (redoStack.length === 0) return;
    const next = redoStack[redoStack.length - 1];
    set({
      redoStack: redoStack.slice(0, -1),
      undoStack: [...get().undoStack, { nodes: [...nodes], edges: [...edges] }],
      nodes: next.nodes,
      edges: next.edges,
    });
  },

  addNode: (name, x, y) => {
    const { pushState, nodes, edges, history } = get();
    pushState();
    const newNode: GraphNode = { id: uuidv4(), name, x, y };
    const newNodes = [...nodes, newNode];
    set({ nodes: newNodes });
    saveToStorage(newNodes, edges, history);

    addNodeToServer(newNode).then(ok => {
      if (ok) console.info(`[Store] Node persisted to MySQL: ${newNode.id}`);
    });
  },

  updateNode: (id, updates) => {
    const { nodes, edges, history } = get();
    const node = nodes.find((n) => n.id === id);
    if (!node) return;
    const newName = updates.name ?? node.name;
    const newNodes = nodes.map((n) => (n.id === id ? { ...n, ...updates } : n));
    set({ nodes: newNodes });
    saveToStorage(newNodes, edges, history);

    updateNodeOnServer(id, newName, updates.x ?? node.x, updates.y ?? node.y).then(ok => {
      if (ok) console.info(`[Store] Node updated in MySQL: ${id}`);
    });
  },

  removeNode: (id) => {
    const { pushState, nodes, edges, history } = get();
    pushState();
    const newNodes = nodes.filter((n) => n.id !== id);
    const newEdges = edges.filter((e) => e.source !== id && e.target !== id);
    set({ nodes: newNodes, edges: newEdges });
    saveToStorage(newNodes, newEdges, history);

    deleteNodeOnServer(id).then(ok => {
      if (ok) console.info(`[Store] Node deleted from MySQL: ${id}`);
    });
  },

  addEdge: (source, target, distance, traffic: TrafficLevel = 'normal') => {
    const { pushState, nodes, edges, history } = get();
    pushState();
    const newEdge: GraphEdge = { id: uuidv4(), source, target, distance, traffic };
    const newEdges = [...edges, newEdge];
    set({ edges: newEdges });
    saveToStorage(nodes, newEdges, history);

    addEdgeToServer(newEdge).then(ok => {
      if (ok) console.info(`[Store] Edge persisted to MySQL: ${newEdge.id}`);
    });
  },

  updateEdge: (id, updates) => {
    const { nodes, edges, history } = get();
    const edge = edges.find((e) => e.id === id);
    if (!edge) return;
    const newSource = updates.source ?? edge.source;
    const newTarget = updates.target ?? edge.target;
    const newDistance = updates.distance ?? edge.distance;
    const newTraffic = updates.traffic ?? edge.traffic;
    const newEdges = edges.map((e) => (e.id === id ? { ...e, ...updates } : e));
    set({ edges: newEdges });
    saveToStorage(nodes, newEdges, history);

    updateEdgeOnServer(id, newSource, newTarget, newDistance, newTraffic).then(ok => {
      if (ok) console.info(`[Store] Edge updated in MySQL: ${id}`);
    });
  },

  removeEdge: (id) => {
    const { pushState, nodes, edges, history } = get();
    pushState();
    const newEdges = edges.filter((e) => e.id !== id);
    set({ edges: newEdges });
    saveToStorage(nodes, newEdges, history);

    deleteEdgeOnServer(id).then(ok => {
      if (ok) console.info(`[Store] Edge deleted from MySQL: ${id}`);
    });
  },

  setTraffic: (edgeId, traffic) => {
    const { nodes, edges, history } = get();
    const edge = edges.find((e) => e.id === edgeId);
    if (!edge) return;
    const newEdges = edges.map((e) => (e.id === edgeId ? { ...e, traffic } : e));
    set({ edges: newEdges });
    saveToStorage(nodes, newEdges, history);

    updateEdgeOnServer(edgeId, edge.source, edge.target, edge.distance, traffic).then(ok => {
      if (ok) console.info(`[Store] Traffic updated in MySQL: ${edgeId} -> ${traffic}`);
    });
  },

  setAlgorithm: (algo) => set({ selectedAlgorithm: algo }),

  findRoute: async (source, destination) => {
    const { nodes, edges, selectedAlgorithm, history } = get();
    let result: AlgorithmResult;

    const startTs = performance.now();
    console.info(`[Store] findRoute algo=${selectedAlgorithm} src=${source} dst=${destination} nodes=${nodes.length} edges=${edges.length}`);

    if (!source || !destination) {
      console.error('[Store] findRoute called with empty source or destination');
      return { path: [], distance: 0, visitedNodes: [], executionTime: 0, pathFound: false };
    }

    const srcNode = nodes.find(n => n.id === source);
    const dstNode = nodes.find(n => n.id === destination);
    if (!srcNode) {
      console.error(`[Store] Source node ${source} not found in graph nodes`);
      return { path: [], distance: 0, visitedNodes: [], executionTime: 0, pathFound: false };
    }
    if (!dstNode) {
      console.error(`[Store] Destination node ${destination} not found in graph nodes`);
      return { path: [], distance: 0, visitedNodes: [], executionTime: 0, pathFound: false };
    }

    try {
      result = await apiFindRoute(selectedAlgorithm, source, destination, nodes, edges);
      console.info(`[Store] Backend result: path=[${result.path.join('->')}] dist=${result.distance.toFixed(2)} visited=${result.visitedNodes.length} time=${result.executionTime.toFixed(2)}ms`);
    } catch (err: any) {
      console.warn(`[Store] Backend unavailable, using client-side algorithm (${selectedAlgorithm}):`, err?.message || err);
      try {
        result = runClientAlgorithm(selectedAlgorithm, nodes, edges, source, destination);
        console.info(`[Store] Client result: path=[${result.path.join('->')}] dist=${result.distance.toFixed(2)} visited=${result.visitedNodes.length} time=${result.executionTime.toFixed(2)}ms`);
      } catch (clientErr: any) {
        console.error(`[Store] Client-side algorithm ${selectedAlgorithm} failed:`, clientErr?.message || clientErr);
        return { path: [], distance: 0, visitedNodes: [], executionTime: performance.now() - startTs, pathFound: false };
      }
    }

    const trafficRecord: Record<string, TrafficLevel> = {};
    for (const e of edges) {
      trafficRecord[e.id] = e.traffic;
    }

    const entry: RouteHistory = {
      id: uuidv4(),
      source,
      destination,
      sourceName: srcNode?.name ?? source,
      destinationName: dstNode?.name ?? destination,
      algorithm: selectedAlgorithm,
      distance: result.distance,
      path: result.path,
      timestamp: Date.now(),
      trafficConditions: trafficRecord,
    };
    const newHistory = [entry, ...history].slice(0, 100);
    set({ history: newHistory });
    saveToStorage(nodes, edges, newHistory);

    // Save algorithm execution to MySQL
    const pathJson = JSON.stringify(result.path);
    const algoUpper = selectedAlgorithm.toUpperCase();
    saveRouteHistoryToServer({
      sourceNode: source,
      destinationNode: destination,
      algorithm: algoUpper,
      distanceKm: result.distance,
      pathJson,
      visitedCount: result.visitedNodes.length,
      executionTimeMs: Math.round(result.executionTime),
      estimatedTravelTimeMin: result.estimatedTravelTime || null,
    }).then((ok) => {
      if (ok) console.info(`[Store] Algorithm execution saved to MySQL: ${algoUpper} ${source}->${destination}`);
    });

    const totalTime = performance.now() - startTs;
    console.info(`[Store] findRoute complete in ${totalTime.toFixed(2)}ms`);
    return result;
  },

  compareAlgorithms: async (source, destination) => {
    const { nodes, edges } = get();
    try {
      return await apiCompareAlgorithms(source, destination, nodes, edges);
    } catch (err) {
      console.warn('Backend unavailable for comparison, using client-side algorithms:', err);
      const algorithms: AlgorithmType[] = ['dijkstra', 'bfs', 'dfs', 'astar', 'bellmanford', 'floydwarshall', 'spfa', 'bidijkstra', 'johnson'];
      const results: Record<string, AlgorithmResult> = {};
      for (const algo of algorithms) {
        const fn = clientAlgos[algo as keyof typeof clientAlgos];
        results[algo] = fn ? fn(nodes, edges, source, destination) : { path: [], distance: 0, visitedNodes: [], executionTime: 0, pathFound: false };
      }
      return results as Record<AlgorithmType, AlgorithmResult>;
    }
  },

  loadSampleData: (data) => {
    const { history } = get();
    set({ nodes: data.nodes, edges: data.edges, undoStack: [], redoStack: [] });
    saveToStorage(data.nodes, data.edges, history);

    saveGraphToServer(data.nodes, data.edges).then(ok => {
      if (ok) console.info(`[Store] Sample data persisted to MySQL: ${data.nodes.length} nodes, ${data.edges.length} edges`);
    });
  },

  clearGraph: () => {
    const { history } = get();
    set({ nodes: [], edges: [], undoStack: [], redoStack: [] });
    saveToStorage([], [], history);

    clearGraphOnServer().then(ok => {
      if (ok) console.info('[Store] Graph cleared from MySQL');
    });
  },

  exportGraph: () => {
    const { nodes, edges } = get();
    return JSON.stringify({ nodes, edges }, null, 2);
  },

  importGraph: (json) => {
    try {
      const data = JSON.parse(json);
      if (data.nodes && data.edges) {
        const { history } = get();
        set({ nodes: data.nodes, edges: data.edges, undoStack: [], redoStack: [] });
        saveToStorage(data.nodes, data.edges, history);

        saveGraphToServer(data.nodes, data.edges).then(ok => {
          if (ok) console.info(`[Store] Graph imported to MySQL: ${data.nodes.length} nodes, ${data.edges.length} edges`);
        });
      }
    } catch (e) {
      console.error('[Store] Failed to import graph:', e);
    }
  },

  clearHistory: () => {
    const { nodes, edges } = get();
    set({ history: [] });
    saveToStorage(nodes, edges, []);

    clearRouteHistoryOnServer().then(ok => {
      if (ok) console.info('[Store] Route history cleared from MySQL');
    });
  },

  saveToServer: async () => {
    const { nodes, edges } = get();
    set({ syncStatus: 'saving', syncMessage: 'Saving to MySQL...' });
    const ok = await saveGraphToServer(nodes, edges);
    set({ syncStatus: ok ? 'saved' : 'error', syncMessage: ok ? 'Graph saved to MySQL' : 'Failed to save graph to MySQL' });
    if (ok) console.info('[Store] Graph persisted to MySQL');
    else console.warn('[Store] Graph persistence to MySQL failed');
    return ok;
  },

  loadFromServer: async () => {
    try {
      set({ syncStatus: 'saving', syncMessage: 'Loading from MySQL...' });
      const data = await loadGraphFromServer();
      if (data && data.nodes.length > 0) {
        const { history } = get();
        set({ nodes: data.nodes, edges: data.edges, undoStack: [], redoStack: [], syncStatus: 'saved', syncMessage: 'Graph loaded from MySQL' });
        saveToStorage(data.nodes, data.edges, history);
        console.info(`[Store] Loaded ${data.nodes.length} nodes, ${data.edges.length} edges from MySQL`);
        return true;
      }
      set({ syncStatus: 'idle', syncMessage: 'No data found in MySQL' });
      return false;
    } catch (e) {
      set({ syncStatus: 'error', syncMessage: 'Failed to load from MySQL' });
      return false;
    }
  },
}));

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'z') {
    e.preventDefault();
    if (e.shiftKey) {
      useGraphStore.getState().redo();
    } else {
      useGraphStore.getState().undo();
    }
  }
});
