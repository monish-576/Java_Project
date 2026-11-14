import { useState, useMemo } from 'react';
import ReactFlow, { Node as RFNode, Edge as RFEdge, Controls, Background, MiniMap, MarkerType } from 'reactflow';
import 'reactflow/dist/style.css';
import { motion } from 'framer-motion';
import { Ambulance, Clock, MapPin, AlertTriangle, Navigation, RefreshCw, Truck, Car, Bike, Shield, Plane, Gauge, Star, ArrowUp, Map, Trash2 } from 'lucide-react';
import { useGraphStore } from '@/lib/store';
import { findRoute } from '@/services/api';
import { runClientAlgorithm } from '@/lib/algorithms';
import { TRAFFIC_COLORS, DEFAULT_VEHICLES, TRAFFIC_SPEED_FACTOR, ALGORITHM_NAMES, AlgorithmType } from '@/types';
import { resolveSampleData } from '@/lib/sampleData';

const SAMPLE_MAPS: Record<string, string> = {
  chandigarh: 'Chandigarh Network',
  campus: 'University Campus',
  ambulance: 'Ambulance Network',
};

const SAVED_GRAPHS_KEY = 'route-saved-graphs-emergency';

function loadSavedGraphs(): Record<string, { nodes: any[]; edges: any[] }> {
  try {
    return JSON.parse(localStorage.getItem(SAVED_GRAPHS_KEY) || '{}');
  } catch { return {}; }
}

const VEHICLE_ICON_MAP: Record<string, React.ElementType> = {
  ambulance: Ambulance,
  truck: Truck,
  car: Car,
  bike: Bike,
  shield: Shield,
  helicopter: Plane,
};

const ALL_ALGORITHMS: AlgorithmType[] = ['dijkstra', 'bfs', 'dfs', 'astar', 'bellmanford', 'floydwarshall', 'spfa', 'bidijkstra', 'johnson'];

export default function EmergencyRouting() {
  const store = useGraphStore();
  const [hospital, setHospital] = useState('');
  const [patient, setPatient] = useState('');
  const [selectedVehicleId, setSelectedVehicleId] = useState('ambulance');
  const [customSpeed, setCustomSpeed] = useState('');
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [savedGraphs, setSavedGraphs] = useState<Record<string, { nodes: any[]; edges: any[] }>>(loadSavedGraphs());

  const handleLoadGraph = (key: string) => {
    setHospital('');
    setPatient('');
    setResult(null);
    setError(null);
    const data = resolveSampleData(key) || (savedGraphs[key] ? { nodes: savedGraphs[key].nodes, edges: savedGraphs[key].edges } : null);
    if (data) {
      store.loadSampleData(data);
    } else {
      setError(`Failed to load map: ${key}`);
    }
  };

  const selectedVehicle = DEFAULT_VEHICLES.find(v => v.id === selectedVehicleId) || DEFAULT_VEHICLES[0];
  const vehicleSpeed = customSpeed ? parseFloat(customSpeed) : selectedVehicle.speed;

  const { trafficFactor, vehicleComparisons, bestVehicle } = useMemo(() => {
    if (!result || !result.distance) {
      return { trafficFactor: 1, vehicleComparisons: [] as any[], bestVehicle: null as any };
    }
    const tf = TRAFFIC_SPEED_FACTOR[result.trafficLevel as keyof typeof TRAFFIC_SPEED_FACTOR] ?? 1;
    const comparisons = DEFAULT_VEHICLES.map(v => {
      const effSpeed = v.speed * tf;
      const eta = effSpeed > 0 ? (result.distance / effSpeed) * 60 : Infinity;
      return { ...v, effectiveSpeed: effSpeed, eta };
    }).sort((a, b) => a.eta - b.eta);
    return { trafficFactor: tf, vehicleComparisons: comparisons, bestVehicle: comparisons[0] };
  }, [result]);

  const pathSet = useMemo(() => {
    if (!result || !result.path) return new Set<string>();
    const set = new Set<string>();
    for (let i = 0; i < result.path.length - 1; i++) {
      const edge = store.edges.find(
        (e) =>
          (e.source === result.path[i] && e.target === result.path[i + 1]) ||
          (e.target === result.path[i] && e.source === result.path[i + 1])
      );
      if (edge) set.add(edge.id);
    }
    return set;
  }, [result, store.edges]);

  const flowNodes: RFNode[] = store.nodes.map((n) => ({
    id: n.id,
    type: 'default',
    position: { x: n.x, y: n.y },
    data: { label: n.name },
    style: {
      background: result?.path?.includes(n.id)
        ? n.id === hospital
          ? '#ef4444'
          : n.id === patient
            ? '#eab308'
            : '#0ea5e9'
        : '#1e293b',
      border: `2px solid ${
        n.id === hospital ? '#ef4444' : n.id === patient ? '#eab308' : result?.path?.includes(n.id) ? '#0ea5e9' : '#334155'
      }`,
      color: '#fff',
      borderRadius: '12px',
      padding: '8px 16px',
      fontWeight: 600,
      fontSize: '13px',
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
      strokeWidth: pathSet.has(e.id) ? 5 : 2,
      opacity: pathSet.has(e.id) ? 1 : 0.6,
      transition: 'all 0.3s',
    },
    markerEnd: { type: MarkerType.ArrowClosed, color: pathSet.has(e.id) ? '#0ea5e9' : TRAFFIC_COLORS[e.traffic] },
  }));

  const handleFindEmergencyRoute = async () => {
    if (!hospital || !patient) {
      setError('Please select both hospital and patient locations.');
      return;
    }
    if (hospital === patient) {
      setError('Hospital and patient location must be different.');
      return;
    }
    if (!vehicleSpeed || vehicleSpeed <= 0) {
      setError('Please enter a valid speed.');
      return;
    }
    setError(null);
    setLoading(true);
    setResult(null);
    const startTs = performance.now();
    console.info(`[EmergencyRouting] Dispatching: hospital=${hospital} patient=${patient} vehicle=${selectedVehicleId} speed=${vehicleSpeed}`);

    try {
      const algoResults: Record<string, any> = {};
      await Promise.all(
        ALL_ALGORITHMS.map(async (algo: AlgorithmType) => {
          try {
            const res = await findRoute(algo, hospital, patient, store.nodes, store.edges);
            algoResults[algo] = res;
          } catch {
            const res = runClientAlgorithm(algo, store.nodes, store.edges, hospital, patient);
            algoResults[algo] = res;
          }
        })
      );

      const validResults = Object.entries(algoResults)
        .filter(([_, r]) => r && r.path && r.path.length > 0)
        .map(([algo, r]) => ({ algo, ...r }));

      if (validResults.length === 0) {
        const hName = store.nodes.find(n => n.id === hospital)?.name || hospital;
        const pName = store.nodes.find(n => n.id === patient)?.name || patient;
        setError(`No reachable path between "${hName}" and "${pName}" with any algorithm. Some roads may be blocked.`);
        setLoading(false);
        return;
      }

      validResults.sort((a, b) => a.distance - b.distance);
      const best = validResults[0];
      const allAlgoResults = Object.fromEntries(
        Object.entries(algoResults).map(([algo, r]) => [
          algo,
          { distance: r?.distance ?? 0,           pathFound: r?.path ? r.path.length > 0 : false, executionTime: r?.executionTime ?? 0 }
        ])
      );

      let maxTraffic: string = 'normal';
      for (let i = 0; i < best.path.length - 1; i++) {
        const edge = store.edges.find(
          (e) =>
            (e.source === best.path[i] && e.target === best.path[i + 1]) ||
            (e.target === best.path[i] && e.source === best.path[i + 1])
        );
        if (edge) {
          const levels = ['blocked', 'heavy', 'moderate', 'normal'];
          if (levels.indexOf(edge.traffic) < levels.indexOf(maxTraffic)) {
            maxTraffic = edge.traffic;
          }
        }
      }

      const tf = TRAFFIC_SPEED_FACTOR[maxTraffic as keyof typeof TRAFFIC_SPEED_FACTOR] ?? 1;
      const effSpeed = vehicleSpeed * tf;
      const time = effSpeed > 0 ? (best.distance / effSpeed) * 60 : Infinity;

      setResult({
        ...best,
        estimatedMinutes: time,
        trafficLevel: maxTraffic,
        speed: vehicleSpeed,
        effectiveSpeed: effSpeed,
        selectedVehicle: selectedVehicle.id,
        bestAlgorithm: best.algo,
        allAlgorithms: allAlgoResults,
      });
      const elapsed = (performance.now() - startTs).toFixed(1);
      console.info(`[EmergencyRouting] Best algo=${best.algo} dist=${best.distance.toFixed(2)}km vehicle=${selectedVehicle.name} speed=${vehicleSpeed}km/h ETA=${time.toFixed(0)}min time=${elapsed}ms`);
    } catch (err: any) {
      const msg = err?.response?.data?.error || err?.message || 'Failed to compute route.';
      console.error('[EmergencyRouting] Error:', err);
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const VehicleIcon = VEHICLE_ICON_MAP[selectedVehicle.icon] || Ambulance;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Emergency Routing</h1>
          <p className="text-surface-400 text-sm mt-1">Fastest emergency response routes</p>
        </div>
      </div>

      <div className="bg-surface-800 rounded-xl p-4 border border-surface-700">
        <h3 className="font-semibold text-sm mb-3 flex items-center gap-2"><Map className="w-4 h-4" /> Select Graph</h3>
        <p className="text-xs text-surface-500 mb-3">Choose a graph. Hospital and patient locations are taken only from this graph.</p>
        <div className="flex flex-wrap gap-2">
          {Object.entries(SAMPLE_MAPS).map(([key, label]) => (
            <button key={key} onClick={() => handleLoadGraph(key)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors border ${
                store.nodes.length > 0 && resolveSampleData(key)?.nodes.every(n => store.nodes.some(sn => sn.id === n.id))
                  ? 'bg-red-600/20 text-red-300 border-red-600/30'
                  : 'bg-surface-900 text-surface-300 border-surface-600 hover:bg-surface-700'
              }`}>
              {label}
            </button>
          ))}
          {Object.entries(savedGraphs).map(([name]) => (
            <div key={name} className="flex gap-1">
              <button onClick={() => handleLoadGraph(name)}
                className="bg-surface-900 hover:bg-surface-700 rounded-lg px-4 py-2 text-sm transition-colors border border-surface-600 text-surface-300">
                {name}
              </button>
              <button onClick={() => {
                const { [name]: _, ...rest } = savedGraphs;
                setSavedGraphs(rest);
                localStorage.setItem(SAVED_GRAPHS_KEY, JSON.stringify(rest));
              }} className="px-2 text-surface-500 hover:text-red-400 transition-colors">
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            </div>
          ))}
        </div>
        {store.nodes.length === 0 && (
          <p className="text-xs text-surface-500 mt-3">No graph loaded. Select one above to begin.</p>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
        <div className="bg-surface-800 rounded-xl p-4 border border-surface-700">
          <h3 className="font-semibold text-sm mb-3 flex items-center gap-2"><MapPin className="w-4 h-4 text-red-400" /> Hospital</h3>
          <select value={hospital} onChange={(e) => setHospital(e.target.value)}
            className="w-full bg-surface-900 border border-surface-600 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-red-500">
            <option value="">Select hospital</option>
            {store.nodes.map((n) => <option key={n.id} value={n.id}>{n.name}</option>)}
          </select>
        </div>
        <div className="bg-surface-800 rounded-xl p-4 border border-surface-700">
          <h3 className="font-semibold text-sm mb-3 flex items-center gap-2"><AlertTriangle className="w-4 h-4 text-yellow-400" /> Patient Location</h3>
          <select value={patient} onChange={(e) => setPatient(e.target.value)}
            className="w-full bg-surface-900 border border-surface-600 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-red-500">
            <option value="">Select patient</option>
            {store.nodes.map((n) => <option key={n.id} value={n.id}>{n.name}</option>)}
          </select>
        </div>
        <div className="bg-surface-800 rounded-xl p-4 border border-surface-700">
          <h3 className="font-semibold text-sm mb-3 flex items-center gap-2"><Gauge className="w-4 h-4 text-emerald-400" /> Vehicle & Speed</h3>
          <select value={selectedVehicleId} onChange={(e) => { setSelectedVehicleId(e.target.value); setCustomSpeed(''); }}
            className="w-full bg-surface-900 border border-surface-600 rounded-lg px-3 py-2 text-sm mb-2 focus:outline-none focus:border-emerald-500">
            {DEFAULT_VEHICLES.map((v) => (
              <option key={v.id} value={v.id}>{v.name} ({v.speed} km/h)</option>
            ))}
          </select>
          <input type="number" value={customSpeed} onChange={(e) => setCustomSpeed(e.target.value)}
            placeholder={`Speed (${selectedVehicle.speed} km/h default)`}
            className="w-full bg-surface-900 border border-surface-600 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-emerald-500" />
        </div>
        <div className="flex items-end">
          <button onClick={handleFindEmergencyRoute} disabled={loading || !hospital || !patient || store.nodes.length === 0}
            className="w-full bg-red-600 hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg py-3 text-sm font-bold transition-colors flex items-center justify-center gap-2">
            {loading ? <RefreshCw className="w-5 h-5 animate-spin" /> : <Ambulance className="w-5 h-5" />}
            {loading ? 'Computing...' : 'Dispatch Emergency'}
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-900/30 border border-red-700 rounded-xl p-3 flex items-start gap-2 text-sm">
          <AlertTriangle className="w-5 h-5 text-red-400 mt-0.5 shrink-0" />
          <span className="text-red-300">{error}</span>
        </div>
      )}

      <div className="bg-surface-800 rounded-xl border border-surface-700 overflow-hidden" style={{ height: '45vh' }}>
        <ReactFlow nodes={flowNodes} edges={flowEdges} fitView className="bg-surface-900">
          <Controls className="bg-surface-800 border-surface-700" />
          <Background gap={20} color="#1e293b" />
          <MiniMap nodeColor="#6366f1" maskColor="rgba(15,23,42,0.8)" className="bg-surface-800 border-surface-700" />
        </ReactFlow>
      </div>

      {result && (
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="space-y-4">
          <div className="bg-emerald-600/10 border border-emerald-600/30 rounded-xl p-4 flex items-center gap-3">
            <Star className="w-6 h-6 text-emerald-400 shrink-0" />
            <div>
              <p className="text-xs text-emerald-400/70">Recommended Algorithm</p>
              <p className="text-lg font-bold text-emerald-300">{ALGORITHM_NAMES[result.bestAlgorithm as AlgorithmType] || result.bestAlgorithm}</p>
              <p className="text-xs text-surface-400 mt-0.5">
                Found shortest path ({result.distance.toFixed(2)} km) after analyzing all 9 algorithms
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
            <div className="bg-red-600/10 border border-red-600/30 rounded-xl p-4">
              <div className="flex items-center gap-3">
                <Clock className="w-8 h-8 text-red-400" />
                <div>
                  <p className="text-surface-400 text-xs">Estimated Arrival</p>
                  <p className="text-xl font-bold text-red-400">{result.estimatedMinutes === Infinity ? '∞' : result.estimatedMinutes.toFixed(0)} <span className="text-sm font-normal">min</span></p>
                </div>
              </div>
              <div className="mt-2 pt-2 border-t border-red-600/20 text-[11px] text-red-300/70 font-mono">
                {result.distance.toFixed(1)}km ÷ ({vehicleSpeed} × {trafficFactor})km/h × 60 = {result.estimatedMinutes === Infinity ? '∞' : result.estimatedMinutes.toFixed(1)}min
              </div>
            </div>
            <div className="bg-surface-800 rounded-xl p-4 border border-surface-700">
              <p className="text-surface-400 text-xs">Distance</p>
              <div className="flex items-baseline gap-1 mt-1">
                <p className="text-xl font-bold">{result.distance.toFixed(1)}</p>
                <span className="text-surface-400 text-sm">km</span>
              </div>
            </div>
            <div className="bg-surface-800 rounded-xl p-4 border border-surface-700">
              <p className="text-surface-400 text-xs">Vehicle</p>
              <div className="flex items-center gap-2 mt-1">
                <VehicleIcon className="w-5 h-5 text-emerald-400" />
                <p className="text-lg font-bold truncate">{selectedVehicle.name}</p>
              </div>
              <p className="text-xs text-surface-500 mt-0.5">{vehicleSpeed} km/h (base)</p>
            </div>
            <div className="bg-surface-800 rounded-xl p-4 border border-surface-700">
              <p className="text-surface-400 text-xs">Traffic Level</p>
              <p className="text-xl font-bold capitalize mt-1">{result.trafficLevel}</p>
              <p className="text-xs text-surface-500 mt-0.5">x{trafficFactor.toFixed(1)} speed factor</p>
            </div>
            <div className="bg-surface-800 rounded-xl p-4 border border-surface-700">
              <p className="text-surface-400 text-xs">Nodes Visited</p>
              <p className="text-xl font-bold mt-1">{result.visitedNodes.length}</p>
              <p className="text-xs text-surface-500 mt-0.5">{result.path.length} nodes in path</p>
            </div>
          </div>

          {result.allAlgorithms && (
            <div className="bg-surface-800 rounded-xl border border-surface-700 overflow-hidden">
              <div className="p-4 border-b border-surface-700">
                <h3 className="font-semibold text-sm flex items-center gap-2"><Navigation className="w-4 h-4 text-sky-400" /> All Algorithms Comparison</h3>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-surface-700 bg-surface-900/50">
                      <th className="text-left p-3 text-xs font-semibold text-surface-400">Algorithm</th>
                      <th className="text-right p-3 text-xs font-semibold text-surface-400">Distance (km)</th>
                      <th className="text-right p-3 text-xs font-semibold text-surface-400">Time (ms)</th>
                      <th className="text-center p-3 text-xs font-semibold text-surface-400">Path Found</th>
                    </tr>
                  </thead>
                  <tbody>
                    {Object.entries(result.allAlgorithms).map(([algo, data]: [string, any]) => {
                      const isBest = algo === result.bestAlgorithm;
                      return (
                        <tr key={algo} className={`border-b border-surface-700/50 ${isBest ? 'bg-emerald-600/10' : ''}`}>
                          <td className="p-3 font-medium flex items-center gap-2">
                            {isBest && <Star className="w-3 h-3 text-emerald-400" />}
                            <span className={isBest ? 'text-emerald-300' : 'text-surface-300'}>{ALGORITHM_NAMES[algo as AlgorithmType] || algo}</span>
                            {isBest && <span className="text-[10px] bg-emerald-600/30 text-emerald-300 px-1.5 py-0.5 rounded-full">Best</span>}
                          </td>
                          <td className="p-3 text-right text-surface-300">{data.pathFound ? data.distance.toFixed(2) : '—'}</td>
                          <td className="p-3 text-right text-surface-300">{data.executionTime.toFixed(1)}</td>
                          <td className="p-3 text-center">
                            {data.pathFound
                              ? <span className="text-green-400 text-xs">Yes</span>
                              : <span className="text-red-400 text-xs">No</span>}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {vehicleComparisons.length > 0 && (
            <div className="bg-surface-800 rounded-xl p-4 border border-surface-700">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-semibold text-sm flex items-center gap-2"><Navigation className="w-4 h-4 text-sky-400" /> Vehicle Comparison</h3>
                {bestVehicle && (
                  <span className="text-xs bg-emerald-600/20 text-emerald-400 px-3 py-1 rounded-full flex items-center gap-1">
                    <Star className="w-3 h-3" /> Best: {bestVehicle.name} ({bestVehicle.eta.toFixed(0)} min)
                  </span>
                )}
              </div>
              <div className="space-y-2">
                {vehicleComparisons.map((v: any, idx: number) => {
                  const isBest = idx === 0;
                  const isSelected = v.id === selectedVehicleId;
                  const Icon = VEHICLE_ICON_MAP[v.icon] || Ambulance;
                  return (
                    <div key={v.id} className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors ${
                      isBest ? 'bg-emerald-600/10 border border-emerald-600/30' :
                      isSelected ? 'bg-primary-600/10 border border-primary-600/30' :
                      'bg-surface-900/50 border border-surface-700/50'
                    }`}>
                      <Icon className={`w-5 h-5 ${isBest ? 'text-emerald-400' : isSelected ? 'text-primary-400' : 'text-surface-400'}`} />
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <span className={`font-medium ${isBest ? 'text-emerald-300' : isSelected ? 'text-primary-300' : 'text-white'}`}>{v.name}</span>
                          {isBest && <span className="text-[10px] bg-emerald-600/30 text-emerald-300 px-2 py-0.5 rounded-full font-semibold flex items-center gap-1"><ArrowUp className="w-2.5 h-2.5" /> Recommended</span>}
                          {isSelected && !isBest && <span className="text-[10px] bg-primary-600/30 text-primary-300 px-2 py-0.5 rounded-full">Selected</span>}
                        </div>
                        <p className="text-xs text-surface-500 truncate">{v.description}</p>
                      </div>
                      <div className="text-right shrink-0">
                        <p className={`text-xs text-surface-400`}>{v.speed} km/h</p>
                        <p className="text-xs text-surface-500">→ {v.effectiveSpeed.toFixed(0)} km/h eff.</p>
                      </div>
                      <div className="text-right shrink-0 min-w-[80px]">
                        <p className={`text-lg font-bold ${isBest ? 'text-emerald-400' : isSelected ? 'text-primary-400' : 'text-white'}`}>
                          {v.eta === Infinity ? '∞' : v.eta.toFixed(0)}
                        </p>
                        <p className="text-[10px] text-surface-600 font-mono">
                          {result.distance.toFixed(1)}÷{v.effectiveSpeed.toFixed(0)}×60
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          <div className="bg-surface-800 rounded-xl p-4 border border-surface-700">
            <h3 className="font-semibold text-sm mb-3 flex items-center gap-2"><Navigation className="w-4 h-4 text-sky-400" /> Emergency Path</h3>
            {result.path.length > 0 ? (
              <div className="flex flex-wrap gap-1.5">
                {result.path.map((id: string, idx: number) => {
                  const n = store.nodes.find((node) => node.id === id);
                  const isHospital = id === hospital;
                  const isPatient = id === patient;
                  return (
                    <span key={id}
                      className={`px-3 py-1 rounded-lg text-sm font-medium flex items-center gap-1 ${
                        isHospital ? 'bg-red-600/20 text-red-300' :
                        isPatient ? 'bg-yellow-600/20 text-yellow-300' :
                        'bg-sky-600/20 text-sky-300'
                      }`}
                    >
                      {isHospital && <Ambulance className="w-3 h-3" />}
                      {isPatient && <MapPin className="w-3 h-3" />}
                      {n?.name ?? id}
                      {idx < result.path.length - 1 && <span className="text-surface-600 mx-0.5">→</span>}
                    </span>
                  );
                })}
              </div>
            ) : (
              <p className="text-red-400">No reachable path! Some roads may be blocked.</p>
            )}
          </div>
        </motion.div>
      )}

      {!result && (
        <div className="bg-surface-800 rounded-xl p-8 border border-surface-700 text-center">
          <Ambulance className="w-16 h-16 mx-auto mb-4 text-surface-600" />
          <p className="text-surface-400">Select a hospital, patient location, and vehicle, then dispatch.</p>
          <p className="text-surface-500 text-sm mt-2">Traffic conditions and vehicle speed are automatically considered.</p>
        </div>
      )}
    </div>
  );
}
