import { useState } from 'react';
import ReactFlow, {
  Node as RFNode,
  Edge as RFEdge,
  Controls,
  Background,
  MiniMap,
  MarkerType,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { motion } from 'framer-motion';
import { Search, Route, Play, RefreshCw, Map, AlertCircle, Clock, Gauge, Trash2, Database, Cloud, CloudOff } from 'lucide-react';
import { useGraphStore } from '@/lib/store';
import { TRAFFIC_COLORS, ALGORITHM_NAMES, DEFAULT_VEHICLES } from '@/types';
import { resolveSampleData } from '@/lib/sampleData';

const SAMPLE_MAPS: Record<string, string> = {
  chandigarh: 'Chandigarh Network',
  campus: 'University Campus',
  ambulance: 'Ambulance Network',
};

const SAVED_GRAPHS_KEY = 'route-saved-graphs';

function loadSavedGraphs(): Record<string, { nodes: any[]; edges: any[] }> {
  try {
    return JSON.parse(localStorage.getItem(SAVED_GRAPHS_KEY) || '{}');
  } catch { return {}; }
}

function saveSavedGraphs(data: Record<string, { nodes: any[]; edges: any[] }>) {
  localStorage.setItem(SAVED_GRAPHS_KEY, JSON.stringify(data));
}

export default function RouteOptimizer() {
  const store = useGraphStore();
  const [source, setSource] = useState('');
  const [dest, setDest] = useState('');
  const [result, setResult] = useState<any>(null);
  const [animating, setAnimating] = useState(false);
  const [visitedSet, setVisitedSet] = useState<Set<string>>(new Set());
  const [pathSet, setPathSet] = useState<Set<string>>(new Set());
  const [error, setError] = useState<string | null>(null);
  const [saveName, setSaveName] = useState('');
  const [savedGraphs, setSavedGraphs] = useState<Record<string, { nodes: any[]; edges: any[] }>>(loadSavedGraphs());
  const [selectedVehicleId, setSelectedVehicleId] = useState('car');
  const [customSpeed, setCustomSpeed] = useState('');

  const selectedVehicle = DEFAULT_VEHICLES.find(v => v.id === selectedVehicleId) || DEFAULT_VEHICLES[0];
  const vehicleSpeed = customSpeed ? parseFloat(customSpeed) : selectedVehicle.speed;

  const handleSaveGraph = () => {
    const name = saveName.trim();
    if (!name || !store.nodes.length) return;
    const updated = { ...savedGraphs, [name]: { nodes: store.nodes, edges: store.edges } };
    setSavedGraphs(updated);
    saveSavedGraphs(updated);
    setSaveName('');
  };

  const handleDeleteSaved = (name: string) => {
    const { [name]: _, ...rest } = savedGraphs;
    setSavedGraphs(rest);
    saveSavedGraphs(rest);
  };

  const flowNodes: RFNode[] = store.nodes.map((n) => ({
    id: n.id,
    type: 'default',
    position: { x: n.x, y: n.y },
    data: {
      label: n.name,
    },
    style: {
      background: visitedSet.has(n.id)
        ? pathSet.has(n.id)
          ? '#0ea5e9'
          : '#6366f180'
        : '#1e293b',
      border: `2px solid ${pathSet.has(n.id) ? '#0ea5e9' : visitedSet.has(n.id) ? '#818cf8' : '#334155'}`,
      color: '#fff',
      borderRadius: '12px',
      padding: '8px 16px',
      fontWeight: 600,
      fontSize: '14px',
      transition: 'all 0.3s',
    },
  }));

  const flowEdges: RFEdge[] = store.edges.map((e) => ({
    id: e.id,
    source: e.source,
    target: e.target,
    label: `${e.distance}km`,
    style: {
      stroke: pathSet.has(e.id) ? '#0ea5e9' : TRAFFIC_COLORS[e.traffic],
      strokeWidth: pathSet.has(e.id) ? 4 : 2,
      transition: 'all 0.3s',
    },
    markerEnd: { type: MarkerType.ArrowClosed, color: pathSet.has(e.id) ? '#0ea5e9' : TRAFFIC_COLORS[e.traffic] },
  }));

    const handleFindRoute = async () => {
    if (!source || !dest) {
      setError('Please select both source and destination nodes.');
      return;
    }
    if (source === dest) {
      setError('Source and destination must be different nodes.');
      return;
    }
    setError(null);
    setAnimating(true);
    setVisitedSet(new Set());
    setPathSet(new Set());
    setResult(null);

    const startTs = performance.now();
    console.info(`[RouteOptimizer] Finding route: algo=${store.selectedAlgorithm} src=${source} dst=${dest}`);

    try {
      const res = await store.findRoute(source, dest);
      if (!res) {
        setError('Failed to compute route - no result returned.');
        setAnimating(false);
        return;
      }

      if (!res.path || res.path.length === 0) {
        const srcName = store.nodes.find(n => n.id === source)?.name || source;
        const dstName = store.nodes.find(n => n.id === dest)?.name || dest;
        setError(`No path found between "${srcName}" and "${dstName}". The destination may be unreachable due to blocked roads.`);
        setAnimating(false);
        return;
      }

      for (let i = 0; i < res.visitedNodes.length; i++) {
        await new Promise((r) => setTimeout(r, 50));
        setVisitedSet(new Set(res.visitedNodes.slice(0, i + 1)));
      }

      await new Promise((r) => setTimeout(r, 300));
      const pathEdgeIds: string[] = [];
      for (let i = 0; i < res.path.length - 1; i++) {
        const edge = store.edges.find(
          (e) =>
            (e.source === res.path[i] && e.target === res.path[i + 1]) ||
            (e.target === res.path[i] && e.source === res.path[i + 1])
        );
        if (edge) pathEdgeIds.push(edge.id);
      }
      setPathSet(new Set(pathEdgeIds));
      setResult(res);

      const elapsed = (performance.now() - startTs).toFixed(1);
      console.info(`[RouteOptimizer] Route found! path=[${res.path.join('->')}] dist=${res.distance.toFixed(2)}km time=${elapsed}ms`);
    } catch (err: any) {
      const msg = err?.response?.data?.error || err?.message || 'Failed to compute route.';
      console.error('[RouteOptimizer] Error:', err);
      setError(msg);
    } finally {
      setAnimating(false);
    }
  };

  const handleLoadSample = (key: string) => {
    setError(null);
    setVisitedSet(new Set());
    setPathSet(new Set());
    setSource('');
    setDest('');
    const data = resolveSampleData(key) || (savedGraphs[key] ? { nodes: savedGraphs[key].nodes, edges: savedGraphs[key].edges } : null);
    if (data) {
      store.loadSampleData(data);
      console.info(`[RouteOptimizer] Loaded: ${SAMPLE_MAPS[key as keyof typeof SAMPLE_MAPS] || key} (${data.nodes.length} nodes, ${data.edges.length} edges)`);
    } else {
      setError(`Failed to load map data for key: ${key}`);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Route Optimizer</h1>
        <p className="text-surface-400 text-sm mt-1">Find the optimal path between two locations</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
        <div className="lg:col-span-3 bg-surface-800 rounded-xl border border-surface-700 overflow-hidden" style={{ height: '55vh' }}>
          <ReactFlow nodes={flowNodes} edges={flowEdges} fitView className="bg-surface-900">
            <Controls className="bg-surface-800 border-surface-700" />
            <Background gap={20} color="#1e293b" />
            <MiniMap nodeColor="#6366f1" maskColor="rgba(15,23,42,0.8)" className="bg-surface-800 border-surface-700" />
          </ReactFlow>
        </div>

        <div className="space-y-4">
          <motion.div initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} className="bg-surface-800 rounded-xl p-4 border border-surface-700">
            <h3 className="font-semibold text-sm mb-3 flex items-center gap-2"><Search className="w-4 h-4" /> Route Settings</h3>

            {error && (
              <div className="bg-red-900/30 border border-red-700 rounded-lg p-2 mb-3 flex items-start gap-2 text-sm">
                <AlertCircle className="w-4 h-4 text-red-400 mt-0.5 shrink-0" />
                <span className="text-red-300">{error}</span>
              </div>
            )}

            <label className="text-xs text-surface-400 mb-1 block">Source</label>
            <select
              value={source}
              onChange={(e) => { setSource(e.target.value); setError(null); setVisitedSet(new Set()); setPathSet(new Set()); setResult(null); }}
              className="w-full bg-surface-900 border border-surface-600 rounded-lg px-3 py-2 text-sm mb-3 focus:outline-none focus:border-primary-500"
            >
              <option value="">Select source</option>
              {store.nodes.map((n) => <option key={n.id} value={n.id}>{n.name}</option>)}
            </select>

            <label className="text-xs text-surface-400 mb-1 block">Destination</label>
            <select
              value={dest}
              onChange={(e) => { setDest(e.target.value); setError(null); setVisitedSet(new Set()); setPathSet(new Set()); setResult(null); }}
              className="w-full bg-surface-900 border border-surface-600 rounded-lg px-3 py-2 text-sm mb-3 focus:outline-none focus:border-primary-500"
            >
              <option value="">Select destination</option>
              {store.nodes.map((n) => <option key={n.id} value={n.id}>{n.name}</option>)}
            </select>

            <label className="text-xs text-surface-400 mb-1 block">Algorithm</label>
            <select
              value={store.selectedAlgorithm}
              onChange={(e) => { store.setAlgorithm(e.target.value as any); setVisitedSet(new Set()); setPathSet(new Set()); setResult(null); }}
              className="w-full bg-surface-900 border border-surface-600 rounded-lg px-3 py-2 text-sm mb-3 focus:outline-none focus:border-primary-500"
            >
              {Object.entries(ALGORITHM_NAMES).map(([key, name]) => (
                <option key={key} value={key}>{name}</option>
              ))}
            </select>

            <label className="text-xs text-surface-400 mb-1 block flex items-center gap-1"><Gauge className="w-3 h-3" /> Vehicle</label>
            <select
              value={selectedVehicleId}
              onChange={(e) => { setSelectedVehicleId(e.target.value); setCustomSpeed(''); }}
              className="w-full bg-surface-900 border border-surface-600 rounded-lg px-3 py-2 text-sm mb-2 focus:outline-none focus:border-primary-500"
            >
              {DEFAULT_VEHICLES.map((v) => (
                <option key={v.id} value={v.id}>{v.name} ({v.speed} km/h)</option>
              ))}
            </select>
            <input type="number" value={customSpeed} onChange={(e) => setCustomSpeed(e.target.value)}
              placeholder={`Speed (${selectedVehicle.speed} km/h default)`}
              className="w-full bg-surface-900 border border-surface-600 rounded-lg px-3 py-2 text-sm mb-3 focus:outline-none focus:border-primary-500" />

            <button
              onClick={handleFindRoute}
              disabled={animating || !source || !dest || store.nodes.length === 0}
              className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg py-2 text-sm font-medium transition-colors flex items-center justify-center gap-2"
            >
              {animating ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Play className="w-4 h-4" />}
              {animating ? 'Computing...' : 'Find Route'}
            </button>
          </motion.div>

          {result && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              className="bg-surface-800 rounded-xl p-4 border border-surface-700"
            >
              <h3 className="font-semibold text-sm mb-3 flex items-center gap-2"><Route className="w-4 h-4 text-sky-400" /> Route Found</h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-surface-400">Algorithm</span>
                  <span className="font-medium">{ALGORITHM_NAMES[store.selectedAlgorithm]}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-surface-400">Distance</span>
                  <span className="font-bold text-sky-400">{result.distance.toFixed(1)} km</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-surface-400">Vehicle</span>
                  <span className="font-medium">{selectedVehicle.name} ({vehicleSpeed} km/h)</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-surface-400">ETA</span>
                  <span className="font-bold text-emerald-400">{((result.distance / vehicleSpeed) * 60).toFixed(0)} min</span>
                </div>
                <div className="text-[10px] text-surface-500 font-mono text-right">
                  {result.distance.toFixed(1)}km ÷ {vehicleSpeed}km/h × 60 = {((result.distance / vehicleSpeed) * 60).toFixed(1)}min
                </div>
                <div className="flex justify-between">
                  <span className="text-surface-400">Visited Nodes</span>
                  <span>{result.visitedNodes.length}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-surface-400">Time</span>
                  <span>{(result.executionTime).toFixed(2)}ms</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-surface-400">Path</span>
                  <span>{result.path.length > 0 ? `${result.path.length} nodes` : 'No path found'}</span>
                </div>
                {result.path.length > 0 && (
                  <div className="mt-2 flex flex-wrap gap-1">
                    {result.path.map((id: string) => {
                      const n = store.nodes.find((node) => node.id === id);
                      return (
                        <span key={id} className="bg-primary-500/20 text-primary-300 px-2 py-0.5 rounded text-xs">
                          {n?.name ?? id}
                        </span>
                      );
                    })}
                  </div>
                )}
              </div>
            </motion.div>
          )}

          <motion.div initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} className="bg-surface-800 rounded-xl p-4 border border-surface-700">
            <h3 className="font-semibold text-sm mb-3 flex items-center gap-2"><Map className="w-4 h-4" /> Load Sample Map</h3>
            <div className="space-y-2">
              {Object.entries(SAMPLE_MAPS).map(([key, label]) => (
                <button
                  key={key}
                  onClick={() => handleLoadSample(key)}
                  className="w-full bg-surface-900 hover:bg-surface-700 rounded-lg py-2 text-sm transition-colors border border-surface-600"
                >
                  {label}
                </button>
              ))}
              {Object.entries(savedGraphs).map(([name, data]) => (
                <div key={name} className="flex gap-1">
                  <button
                    onClick={() => handleLoadSample(name)}
                    className="flex-1 bg-surface-900 hover:bg-surface-700 rounded-lg py-2 text-sm transition-colors border border-surface-600 text-left px-3 truncate"
                  >
                    {name}
                  </button>
                  <button
                    onClick={() => handleDeleteSaved(name)}
                    className="px-2 py-1 text-surface-500 hover:text-red-400 transition-colors"
                    title="Delete"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
            <div className="flex gap-2 mt-3">
              <input
                value={saveName}
                onChange={(e) => setSaveName(e.target.value)}
                placeholder="Save current graph as..."
                className="flex-1 bg-surface-900 border border-surface-600 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary-500"
                onKeyDown={(e) => e.key === 'Enter' && handleSaveGraph()}
              />
              <button
                onClick={handleSaveGraph}
                disabled={!saveName.trim() || !store.nodes.length}
                className="bg-primary-600 hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg px-3 py-2 text-sm transition-colors"
              >
                Save
              </button>
            </div>
          </motion.div>

          <motion.div initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} className="bg-surface-800 rounded-xl p-4 border border-surface-700">
            <h3 className="font-semibold text-sm mb-3 flex items-center gap-2"><Database className="w-4 h-4" /> MySQL Persistence</h3>
            <div className="flex gap-2">
              <button
                onClick={() => store.saveToServer()}
                disabled={store.nodes.length === 0}
                className="flex-1 bg-primary-600 hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg py-2 text-xs font-medium transition-colors flex items-center justify-center gap-1"
              >
                <Database className="w-3.5 h-3.5" /> Save
              </button>
              <button
                onClick={() => store.loadFromServer()}
                className="flex-1 bg-surface-900 hover:bg-surface-700 rounded-lg py-2 text-xs transition-colors border border-surface-600 flex items-center justify-center gap-1"
              >
                <RefreshCw className="w-3.5 h-3.5" /> Load
              </button>
            </div>
            {store.syncStatus === 'saving' && <p className="text-xs text-yellow-400 mt-2 flex items-center gap-1"><RefreshCw className="w-3 h-3 animate-spin" /> {store.syncMessage || 'Saving...'}</p>}
            {store.syncStatus === 'saved' && <p className="text-xs text-green-400 mt-2 flex items-center gap-1"><Cloud className="w-3 h-3" /> {store.syncMessage || 'Saved to MySQL'}</p>}
            {store.syncStatus === 'error' && <p className="text-xs text-red-400 mt-2 flex items-center gap-1"><CloudOff className="w-3 h-3" /> {store.syncMessage || 'Sync failed'}</p>}
          </motion.div>

          {store.history.length > 0 && (
            <motion.div initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} className="bg-surface-800 rounded-xl p-4 border border-surface-700">
              <div className="flex items-center justify-between mb-3">
                <h3 className="font-semibold text-sm flex items-center gap-2"><Clock className="w-4 h-4" /> Route History</h3>
                <button onClick={() => store.clearHistory()} className="text-surface-500 hover:text-red-400 transition-colors">
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
              <div className="space-y-2 max-h-48 overflow-auto">
                {store.history.slice(0, 10).map((h) => (
                    <div key={h.id} className="bg-surface-900/50 rounded-lg p-2.5 text-xs border border-surface-700/50">
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-white font-medium truncate">
                          {h.sourceName ?? h.source} → {h.destinationName ?? h.destination}
                        </span>
                        <span className="text-sky-400 font-semibold ml-2 shrink-0">{h.distance.toFixed(1)} km</span>
                      </div>
                      <div className="flex items-center gap-2 text-surface-400">
                        <span className="capitalize">{h.algorithm}</span>
                        <span>·</span>
                        <span>{new Date(h.timestamp).toLocaleDateString()}</span>
                      </div>
                    </div>
                  ))}
              </div>
            </motion.div>
          )}

          {!store.nodes.length && (
            <div className="bg-surface-800 rounded-xl p-4 border border-surface-700 text-center text-surface-500 text-sm">
              <Route className="w-8 h-8 mx-auto mb-2 opacity-50" />
              <p>No graph data. Load a sample map above or build a graph in the Graph Builder.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
