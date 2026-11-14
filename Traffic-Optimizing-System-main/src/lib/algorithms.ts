import { GraphNode, GraphEdge, AlgorithmResult, TRAFFIC_MULTIPLIER } from '@/types';

interface WeightedNeighbor {
  nodeId: string;
  weight: number;
}

function buildAdjacencyList(nodes: GraphNode[], edges: GraphEdge[]): Map<string, WeightedNeighbor[]> {
  const adj = new Map<string, WeightedNeighbor[]>();
  for (const node of nodes) {
    adj.set(node.id, []);
  }
  for (const edge of edges) {
    if (!edge.source || !edge.target) {
      console.warn(`[buildAdjacencyList] Skipping edge with empty source/target: ${edge.id}`);
      continue;
    }
    if (!adj.has(edge.source)) {
      console.warn(`[buildAdjacencyList] Edge ${edge.id} references unknown source node: ${edge.source}`);
      continue;
    }
    if (!adj.has(edge.target)) {
      console.warn(`[buildAdjacencyList] Edge ${edge.id} references unknown target node: ${edge.target}`);
      continue;
    }
    const multiplier = TRAFFIC_MULTIPLIER[edge.traffic];
    const effectiveDist = multiplier === Infinity ? Infinity : edge.distance * multiplier;
    if (effectiveDist !== Infinity) {
      adj.get(edge.source)!.push({ nodeId: edge.target, weight: effectiveDist });
      adj.get(edge.target)!.push({ nodeId: edge.source, weight: effectiveDist });
    }
  }
  return adj;
}

function reconstructPath(prev: Map<string, string | null>, source: string, destination: string): string[] {
  const path: string[] = [];
  let current: string | null = destination;
  while (current !== null) {
    path.push(current);
    current = prev.get(current) ?? null;
  }
  path.reverse();
  if (path.length === 0 || path[0] !== source) return [];
  return path;
}

function logAlgorithmResult(algorithm: string, source: string, destination: string, result: AlgorithmResult) {
  console.info(
    `[${algorithm}] src=${source} dst=${destination} ` +
    `pathFound=${result.path.length > 0} path=[${result.path.join('->')}] ` +
    `distance=${result.distance.toFixed(2)} visited=${result.visitedNodes.length} ` +
    `time=${result.executionTime.toFixed(2)}ms`
  );
}

function runDijkstra(nodes: GraphNode[], edges: GraphEdge[], source: string, destination: string): AlgorithmResult {
  const startTime = performance.now();
  const adj = buildAdjacencyList(nodes, edges);

  if (!adj.has(source) || !adj.has(destination)) {
    console.warn(`[dijkstra] Source or destination node not found in graph: src=${source} dst=${destination}`);
    return { path: [], distance: 0, visitedNodes: [], executionTime: 0 };
  }

  const dist = new Map<string, number>();
  const prev = new Map<string, string | null>();
  const visited: string[] = [];

  for (const node of nodes) {
    dist.set(node.id, Infinity);
    prev.set(node.id, null);
  }
  dist.set(source, 0);

  const pq: { nodeId: string; distance: number }[] = [{ nodeId: source, distance: 0 }];

  while (pq.length > 0) {
    pq.sort((a, b) => a.distance - b.distance);
    const { nodeId: currentId } = pq.shift()!;

    if (visited.includes(currentId)) continue;
    visited.push(currentId);

    if (currentId === destination) break;

    for (const neighbor of adj.get(currentId) || []) {
      if (visited.includes(neighbor.nodeId)) continue;
      const currentDist = dist.get(currentId);
      if (currentDist === undefined || currentDist === Infinity) continue;
      const alt = currentDist + neighbor.weight;
      if (alt < (dist.get(neighbor.nodeId) ?? Infinity)) {
        dist.set(neighbor.nodeId, alt);
        prev.set(neighbor.nodeId, currentId);
        pq.push({ nodeId: neighbor.nodeId, distance: alt });
      }
    }
  }

  const path = reconstructPath(prev, source, destination);
  const totalDistance = dist.get(destination) ?? Infinity;
  const pathFound = path.length > 0 && totalDistance !== Infinity;
  const execTime = performance.now() - startTime;

  const result: AlgorithmResult = {
    path: pathFound ? path : [],
    distance: pathFound ? totalDistance : 0,
    visitedNodes: visited,
    executionTime: execTime,
    pathFound,
  };
  logAlgorithmResult('dijkstra', source, destination, result);
  return result;
}

function runBFS(nodes: GraphNode[], edges: GraphEdge[], source: string, destination: string): AlgorithmResult {
  const startTime = performance.now();
  const adj = buildAdjacencyList(nodes, edges);

  if (!adj.has(source) || !adj.has(destination)) {
    console.warn(`[bfs] Source or destination node not found in graph: src=${source} dst=${destination}`);
    return { path: [], distance: 0, visitedNodes: [], executionTime: 0 };
  }

  const visited = new Set<string>();
  const visitedOrder: string[] = [];
  const prev = new Map<string, string | null>();
  const dist = new Map<string, number>();
  const queue: string[] = [];

  visited.add(source);
  visitedOrder.push(source);
  dist.set(source, 0);
  prev.set(source, null);
  queue.push(source);

  while (queue.length > 0) {
    const current = queue.shift()!;
    if (current === destination) break;

    for (const neighbor of adj.get(current) || []) {
      if (!visited.has(neighbor.nodeId)) {
        visited.add(neighbor.nodeId);
        visitedOrder.push(neighbor.nodeId);
        prev.set(neighbor.nodeId, current);
        dist.set(neighbor.nodeId, (dist.get(current) || 0) + neighbor.weight);
        queue.push(neighbor.nodeId);
      }
    }
  }

  const path = reconstructPath(prev, source, destination);
  const totalDistance = dist.get(destination) ?? 0;
  const pathFound = path.length > 0;
  const execTime = performance.now() - startTime;

  const result: AlgorithmResult = {
    path: pathFound ? path : [],
    distance: pathFound ? totalDistance : 0,
    visitedNodes: visitedOrder,
    executionTime: execTime,
    pathFound,
  };
  logAlgorithmResult('bfs', source, destination, result);
  return result;
}

function runDFS(nodes: GraphNode[], edges: GraphEdge[], source: string, destination: string): AlgorithmResult {
  const startTime = performance.now();
  const adj = buildAdjacencyList(nodes, edges);

  if (!adj.has(source) || !adj.has(destination)) {
    console.warn(`[dfs] Source or destination node not found in graph: src=${source} dst=${destination}`);
    return { path: [], distance: 0, visitedNodes: [], executionTime: 0 };
  }

  const visitedSet = new Set<string>();
  const visitedOrder: string[] = [];
  const prev = new Map<string, string | null>();
  const dist = new Map<string, number>();
  let found = false;

  function dfs(current: string, parent: string | null, currentDist: number) {
    if (found) return;
    visitedSet.add(current);
    visitedOrder.push(current);
    prev.set(current, parent);
    dist.set(current, currentDist);

    if (current === destination) {
      found = true;
      return;
    }

    for (const neighbor of adj.get(current) || []) {
      if (!visitedSet.has(neighbor.nodeId)) {
        dfs(neighbor.nodeId, current, currentDist + neighbor.weight);
      }
    }
  }

  dfs(source, null, 0);

  const path = reconstructPath(prev, source, destination);
  const totalDistance = dist.get(destination) ?? 0;
  const pathFound = path.length > 0;
  const execTime = performance.now() - startTime;

  const result: AlgorithmResult = {
    path: pathFound ? path : [],
    distance: pathFound ? totalDistance : 0,
    visitedNodes: visitedOrder,
    executionTime: execTime,
    pathFound,
  };
  logAlgorithmResult('dfs', source, destination, result);
  return result;
}

function heuristic(a: GraphNode | undefined, b: GraphNode | undefined): number {
  if (!a || !b) return 0;
  const dx = a.x - b.x;
  const dy = a.y - b.y;
  return Math.sqrt(dx * dx + dy * dy);
}

function runAStar(nodes: GraphNode[], edges: GraphEdge[], source: string, destination: string): AlgorithmResult {
  const startTime = performance.now();
  const adj = buildAdjacencyList(nodes, edges);

  if (!adj.has(source) || !adj.has(destination)) {
    console.warn(`[astar] Source or destination node not found in graph: src=${source} dst=${destination}`);
    return { path: [], distance: 0, visitedNodes: [], executionTime: 0 };
  }

  const destNode = nodes.find(n => n.id === destination);
  if (!destNode) {
    console.warn(`[astar] Destination node ${destination} not found in nodes list`);
    return { path: [], distance: 0, visitedNodes: [], executionTime: 0 };
  }

  const gScore = new Map<string, number>();
  const fScore = new Map<string, number>();
  const prev = new Map<string, string | null>();
  const closedSet = new Set<string>();
  const visitedOrder: string[] = [];

  for (const node of nodes) {
    gScore.set(node.id, Infinity);
    fScore.set(node.id, Infinity);
  }
  gScore.set(source, 0);
  const srcNode = nodes.find(n => n.id === source);
  fScore.set(source, heuristic(srcNode, destNode));

  const openSet: { nodeId: string; fScore: number }[] = [{ nodeId: source, fScore: fScore.get(source)! }];

  while (openSet.length > 0) {
    openSet.sort((a, b) => a.fScore - b.fScore);
    const { nodeId: current } = openSet.shift()!;

    if (closedSet.has(current)) continue;
    closedSet.add(current);
    visitedOrder.push(current);

    if (current === destination) {
      const path = reconstructPath(prev, source, destination);
      const totalDistance = gScore.get(destination) ?? 0;
      const pathFound = path.length > 0;
      const execTime = performance.now() - startTime;

      const result: AlgorithmResult = {
        path: pathFound ? path : [],
        distance: pathFound ? totalDistance : 0,
        visitedNodes: visitedOrder,
        executionTime: execTime,
        pathFound,
      };
      logAlgorithmResult('astar', source, destination, result);
      return result;
    }

    for (const neighbor of adj.get(current) || []) {
      if (closedSet.has(neighbor.nodeId)) continue;
      const currentG = gScore.get(current);
      if (currentG === undefined || currentG === Infinity) continue;
      const tentativeG = currentG + neighbor.weight;
      const neighborG = gScore.get(neighbor.nodeId);
      if (neighborG === undefined) continue;
      if (tentativeG < neighborG) {
        prev.set(neighbor.nodeId, current);
        gScore.set(neighbor.nodeId, tentativeG);
        const neighborNode = nodes.find(n => n.id === neighbor.nodeId);
        fScore.set(neighbor.nodeId, tentativeG + heuristic(neighborNode, destNode));
        openSet.push({ nodeId: neighbor.nodeId, fScore: fScore.get(neighbor.nodeId)! });
      }
    }
  }

  const execTime = performance.now() - startTime;
  const result: AlgorithmResult = { path: [], distance: 0, visitedNodes: visitedOrder, executionTime: execTime, pathFound: false };
  logAlgorithmResult('astar', source, destination, result);
  return result;
}

function runBellmanFord(nodes: GraphNode[], edges: GraphEdge[], source: string, destination: string): AlgorithmResult {
  const startTime = performance.now();
  const adj = buildAdjacencyList(nodes, edges);
  if (!adj.has(source) || !adj.has(destination)) {
    return { path: [], distance: 0, visitedNodes: [], executionTime: 0 };
  }

  const dist = new Map<string, number>();
  const prev = new Map<string, string | null>();
  const V = nodes.length;

  for (const node of nodes) { dist.set(node.id, Infinity); prev.set(node.id, null); }
  dist.set(source, 0);

  const rawEdges: { src: string; dst: string; w: number }[] = [];
  for (const [src, neighbors] of adj) {
    for (const nb of neighbors) rawEdges.push({ src, dst: nb.nodeId, w: nb.weight });
  }

  for (let i = 0; i < V - 1; i++) {
    let updated = false;
    for (const e of rawEdges) {
      const d = dist.get(e.src);
      const nd = dist.get(e.dst) ?? Infinity;
      if (d !== undefined && d !== Infinity && d + e.w < nd) {
        dist.set(e.dst, d + e.w);
        prev.set(e.dst, e.src);
        updated = true;
      }
    }
    if (!updated) break;
  }

  for (const e of rawEdges) {
    const d = dist.get(e.src);
    const nd = dist.get(e.dst) ?? Infinity;
    if (d !== undefined && d !== Infinity && d + e.w < nd) {
      return { path: [], distance: 0, visitedNodes: [], executionTime: performance.now() - startTime, pathFound: false };
    }
  }

  const path = reconstructPath(prev, source, destination);
  const totalDistance = dist.get(destination) ?? Infinity;
  const pathFound = path.length > 0 && totalDistance !== Infinity;
  const visited: string[] = [];
  for (const node of nodes) { if (dist.get(node.id) !== Infinity) visited.push(node.id); }
  const execTime = performance.now() - startTime;
  const result: AlgorithmResult = { path: pathFound ? path : [], distance: pathFound ? totalDistance : 0, visitedNodes: visited, executionTime: execTime, pathFound };
  logAlgorithmResult('bellmanford', source, destination, result);
  return result;
}

function runSPFA(nodes: GraphNode[], edges: GraphEdge[], source: string, destination: string): AlgorithmResult {
  const startTime = performance.now();
  const adj = buildAdjacencyList(nodes, edges);
  if (!adj.has(source) || !adj.has(destination)) {
    return { path: [], distance: 0, visitedNodes: [], executionTime: 0 };
  }

  const dist = new Map<string, number>();
  const prev = new Map<string, string | null>();
  const inQueueCount = new Map<string, number>();
  const inQueue = new Set<string>();
  const visitedOrder: string[] = [];
  const V = nodes.length;

  for (const node of nodes) { dist.set(node.id, Infinity); prev.set(node.id, null); inQueueCount.set(node.id, 0); }
  dist.set(source, 0);

  const queue: string[] = [source];
  inQueue.add(source);
  inQueueCount.set(source, 1);
  let negativeCycle = false;

  while (queue.length > 0) {
    const current = queue.shift()!;
    inQueue.delete(current);
    visitedOrder.push(current);

    for (const nb of adj.get(current) || []) {
      const d = dist.get(current);
      if (d === undefined || d === Infinity) continue;
      const nd = dist.get(nb.nodeId) ?? Infinity;
      if (d + nb.weight < nd) {
        dist.set(nb.nodeId, d + nb.weight);
        prev.set(nb.nodeId, current);
        if (!inQueue.has(nb.nodeId)) {
          queue.push(nb.nodeId);
          inQueue.add(nb.nodeId);
          const count = (inQueueCount.get(nb.nodeId) ?? 0) + 1;
          inQueueCount.set(nb.nodeId, count);
          if (count >= V) { negativeCycle = true; break; }
        }
      }
    }
    if (negativeCycle) break;
  }

  if (negativeCycle) {
    return { path: [], distance: 0, visitedNodes: visitedOrder, executionTime: performance.now() - startTime, pathFound: false };
  }

  const path = reconstructPath(prev, source, destination);
  const totalDistance = dist.get(destination) ?? Infinity;
  const pathFound = path.length > 0 && totalDistance !== Infinity;
  const execTime = performance.now() - startTime;
  const result: AlgorithmResult = { path: pathFound ? path : [], distance: pathFound ? totalDistance : 0, visitedNodes: visitedOrder, executionTime: execTime, pathFound };
  logAlgorithmResult('spfa', source, destination, result);
  return result;
}

function runFloydWarshall(nodes: GraphNode[], edges: GraphEdge[], source: string, destination: string): AlgorithmResult {
  const startTime = performance.now();
  const adj = buildAdjacencyList(nodes, edges);
  if (!adj.has(source) || !adj.has(destination)) {
    return { path: [], distance: 0, visitedNodes: [], executionTime: 0 };
  }

  const n = nodes.length;
  const nodeIndex = new Map<string, number>();
  const indexToNode: string[] = [];
  nodes.forEach((node, i) => { nodeIndex.set(node.id, i); indexToNode.push(node.id); });

  const dist: number[][] = Array.from({ length: n }, () => Array(n).fill(Infinity));
  const next: number[][] = Array.from({ length: n }, () => Array(n).fill(-1));

  for (let i = 0; i < n; i++) { dist[i][i] = 0; next[i][i] = i; }

  for (const [src, neighbors] of adj) {
    const u = nodeIndex.get(src)!;
    for (const nb of neighbors) {
      const v = nodeIndex.get(nb.nodeId)!;
      if (nb.weight < dist[u][v]) { dist[u][v] = nb.weight; next[u][v] = v; }
    }
  }

  for (let k = 0; k < n; k++) {
    for (let i = 0; i < n; i++) {
      if (dist[i][k] === Infinity) continue;
      for (let j = 0; j < n; j++) {
        if (dist[k][j] === Infinity) continue;
        if (dist[i][k] + dist[k][j] < dist[i][j]) {
          dist[i][j] = dist[i][k] + dist[k][j];
          next[i][j] = next[i][k];
        }
      }
    }
  }

  const srcIdx = nodeIndex.get(source)!;
  const dstIdx = nodeIndex.get(destination)!;
  const totalDistance = dist[srcIdx][dstIdx];
  const pathFound = totalDistance !== Infinity;

  let path: string[] = [];
  if (pathFound) {
    path = [indexToNode[srcIdx]];
    let u = srcIdx;
    while (u !== dstIdx) { u = next[u][dstIdx]; path.push(indexToNode[u]); }
  }

  const visited: string[] = [];
  for (let i = 0; i < n; i++) { if (dist[srcIdx][i] !== Infinity) visited.push(indexToNode[i]); }
  const execTime = performance.now() - startTime;
  const result: AlgorithmResult = { path: pathFound ? path : [], distance: pathFound ? totalDistance : 0, visitedNodes: visited, executionTime: execTime, pathFound };
  logAlgorithmResult('floydwarshall', source, destination, result);
  return result;
}

function runBidirectionalDijkstra(nodes: GraphNode[], edges: GraphEdge[], source: string, destination: string): AlgorithmResult {
  const startTime = performance.now();
  const adj = buildAdjacencyList(nodes, edges);
  if (!adj.has(source) || !adj.has(destination)) {
    return { path: [], distance: 0, visitedNodes: [], executionTime: 0 };
  }
  if (source === destination) {
    return { path: [source], distance: 0, visitedNodes: [source], executionTime: 0, pathFound: true };
  }

  const distF = new Map<string, number>();
  const distB = new Map<string, number>();
  const prevF = new Map<string, string | null>();
  const prevB = new Map<string, string | null>();
  const visitedF = new Set<string>();
  const visitedB = new Set<string>();

  for (const node of nodes) { distF.set(node.id, Infinity); distB.set(node.id, Infinity); prevF.set(node.id, null); prevB.set(node.id, null); }
  distF.set(source, 0);
  distB.set(destination, 0);

  const pqF: { id: string; d: number }[] = [{ id: source, d: 0 }];
  const pqB: { id: string; d: number }[] = [{ id: destination, d: 0 }];

  let meeting: string | null = null;
  let bestDist = Infinity;

  while (pqF.length > 0 && pqB.length > 0) {
    if (meeting !== null) {
      pqF.sort((a, b) => a.d - b.d);
      pqB.sort((a, b) => a.d - b.d);
      if ((pqF[0]?.d ?? Infinity) + (pqB[0]?.d ?? Infinity) >= bestDist) break;
    }

    pqF.sort((a, b) => a.d - b.d);
    const curF = pqF.shift()!;
    if (visitedF.has(curF.id)) continue;
    visitedF.add(curF.id);
    if (visitedB.has(curF.id)) {
      const d = (distF.get(curF.id) ?? Infinity) + (distB.get(curF.id) ?? Infinity);
      if (d < bestDist) { bestDist = d; meeting = curF.id; }
    }
    for (const nb of adj.get(curF.id) || []) {
      if (visitedF.has(nb.nodeId)) continue;
      const alt = (distF.get(curF.id) ?? Infinity) + nb.weight;
      if (alt < (distF.get(nb.nodeId) ?? Infinity)) {
        distF.set(nb.nodeId, alt);
        prevF.set(nb.nodeId, curF.id);
        pqF.push({ id: nb.nodeId, d: alt });
      }
    }

    pqB.sort((a, b) => a.d - b.d);
    const curB = pqB.shift()!;
    if (visitedB.has(curB.id)) continue;
    visitedB.add(curB.id);
    if (visitedF.has(curB.id)) {
      const d = (distF.get(curB.id) ?? Infinity) + (distB.get(curB.id) ?? Infinity);
      if (d < bestDist) { bestDist = d; meeting = curB.id; }
    }
    for (const nb of adj.get(curB.id) || []) {
      if (visitedB.has(nb.nodeId)) continue;
      const alt = (distB.get(curB.id) ?? Infinity) + nb.weight;
      if (alt < (distB.get(nb.nodeId) ?? Infinity)) {
        distB.set(nb.nodeId, alt);
        prevB.set(nb.nodeId, curB.id);
        pqB.push({ id: nb.nodeId, d: alt });
      }
    }
  }

  const pathFound = meeting !== null;
  let path: string[] = [];
  if (pathFound) {
    const fwd: string[] = [];
    let c = meeting;
    while (c) { fwd.push(c); c = prevF.get(c) ?? null; }
    fwd.reverse();
    const bwd: string[] = [];
    c = prevB.get(meeting!) ?? null;
    while (c) { bwd.push(c); c = prevB.get(c) ?? null; }
    path = [...fwd, ...bwd];
    if (path[0] !== source) path = [];
  }
  const allVisited = [...visitedF, ...visitedB];
  const execTime = performance.now() - startTime;
  const result: AlgorithmResult = { path: pathFound ? path : [], distance: pathFound ? bestDist : 0, visitedNodes: allVisited, executionTime: execTime, pathFound };
  logAlgorithmResult('bidijkstra', source, destination, result);
  return result;
}

function runJohnson(nodes: GraphNode[], edges: GraphEdge[], source: string, destination: string): AlgorithmResult {
  const startTime = performance.now();
  const adj = buildAdjacencyList(nodes, edges);
  if (!adj.has(source) || !adj.has(destination)) {
    return { path: [], distance: 0, visitedNodes: [], executionTime: 0 };
  }

  const V = nodes.length;
  const potential = new Map<string, number>();

  for (const node of nodes) potential.set(node.id, Infinity);
  const virtual = '__virtual__';
  potential.set(virtual, 0);

  const rawEdges: { src: string; dst: string; w: number }[] = [];
  for (const [src, neighbors] of adj) {
    for (const nb of neighbors) rawEdges.push({ src, dst: nb.nodeId, w: nb.weight });
  }
  for (const node of nodes) rawEdges.push({ src: virtual, dst: node.id, w: 0 });

  for (let i = 0; i < V; i++) {
    let updated = false;
    for (const e of rawEdges) {
      const d = potential.get(e.src);
      const nd = potential.get(e.dst) ?? Infinity;
      if (d !== undefined && d !== Infinity && d + e.w < nd) {
        potential.set(e.dst, d + e.w);
        updated = true;
      }
    }
    if (!updated) break;
  }

  for (const e of rawEdges) {
    const d = potential.get(e.src);
    const nd = potential.get(e.dst) ?? Infinity;
    if (d !== undefined && d !== Infinity && d + e.w < nd) {
      return { path: [], distance: 0, visitedNodes: [], executionTime: performance.now() - startTime, pathFound: false };
    }
  }

  const reweightedAdj = new Map<string, { nodeId: string; weight: number }[]>();
  for (const [src, neighbors] of adj) {
    const rw: { nodeId: string; weight: number }[] = [];
    for (const nb of neighbors) {
      rw.push({ nodeId: nb.nodeId, weight: nb.weight + (potential.get(src) ?? 0) - (potential.get(nb.nodeId) ?? 0) });
    }
    reweightedAdj.set(src, rw);
  }

  const dist = new Map<string, number>();
  const prev = new Map<string, string | null>();
  const visited: string[] = [];

  for (const node of nodes) { dist.set(node.id, Infinity); prev.set(node.id, null); }
  dist.set(source, 0);

  const pq: { id: string; d: number }[] = [{ id: source, d: 0 }];
  while (pq.length > 0) {
    pq.sort((a, b) => a.d - b.d);
    const { id: cur } = pq.shift()!;
    if (visited.includes(cur)) continue;
    visited.push(cur);
    if (cur === destination) break;
    for (const nb of reweightedAdj.get(cur) || []) {
      if (visited.includes(nb.nodeId)) continue;
      const alt = (dist.get(cur) ?? Infinity) + nb.weight;
      if (alt < (dist.get(nb.nodeId) ?? Infinity)) {
        dist.set(nb.nodeId, alt);
        prev.set(nb.nodeId, cur);
        pq.push({ id: nb.nodeId, d: alt });
      }
    }
  }

  const path = reconstructPath(prev, source, destination);
  const reweightedDist = dist.get(destination) ?? Infinity;
  const pathFound = path.length > 0 && reweightedDist !== Infinity;
  const actualDist = pathFound ? reweightedDist - (potential.get(source) ?? 0) + (potential.get(destination) ?? 0) : 0;
  const execTime = performance.now() - startTime;
  const result: AlgorithmResult = { path: pathFound ? path : [], distance: actualDist, visitedNodes: visited, executionTime: execTime, pathFound };
  logAlgorithmResult('johnson', source, destination, result);
  return result;
}

export const clientAlgorithms = {
  dijkstra: runDijkstra,
  bfs: runBFS,
  dfs: runDFS,
  astar: runAStar,
  bellmanford: runBellmanFord,
  spfa: runSPFA,
  floydwarshall: runFloydWarshall,
  bidijkstra: runBidirectionalDijkstra,
  johnson: runJohnson,
};

export function runClientAlgorithm(
  algorithm: string,
  nodes: GraphNode[],
  edges: GraphEdge[],
  source: string,
  destination: string
): AlgorithmResult {
  const fn = clientAlgorithms[algorithm as keyof typeof clientAlgorithms];
  if (!fn) throw new Error('Unknown algorithm: ' + algorithm);
  return fn(nodes, edges, source, destination);
}
