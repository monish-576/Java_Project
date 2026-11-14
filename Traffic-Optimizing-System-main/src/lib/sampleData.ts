import { GraphNode, GraphEdge } from '@/types';
import { v4 as uuidv4 } from 'uuid';

function makeNodes(names: string[], positions: [number, number][]): GraphNode[] {
  return names.map((name, i) => ({
    id: uuidv4(),
    name,
    x: positions[i][0],
    y: positions[i][1],
  }));
}

function makeEdges(srcNames: string[], dstNames: string[], dists: number[], traffics: string[], nodes: GraphNode[]): GraphEdge[] {
  const nodeMap = new Map<string, string>();
  for (const n of nodes) nodeMap.set(n.name, n.id);
  return srcNames.map((src, i) => ({
    id: uuidv4(),
    source: nodeMap.get(src) || '',
    target: nodeMap.get(dstNames[i]) || '',
    distance: dists[i],
    traffic: traffics[i] as any,
  }));
}

const NODES_CHANDIGARH = [
  'Chandigarh', 'Mohali', 'Panchkula', 'Zirakpur',
  'Kharar', 'Dera Bassi', 'Ambala', 'Patiala',
];
const POS_CHANDIGARH: [number, number][] = [
  [400, 200], [250, 300], [550, 250], [300, 400],
  [150, 200], [200, 500], [600, 400], [100, 450],
];

const NODES_CAMPUS = [
  'Main Gate', 'Admin Block', 'Library', 'Science Block',
  'Cafeteria', 'Hostel', 'Sports Complex', 'Auditorium',
];
const POS_CAMPUS: [number, number][] = [
  [300, 500], [400, 400], [500, 300], [300, 250],
  [450, 500], [200, 350], [550, 450], [400, 150],
];

const NODES_AMBULANCE = [
  'City Hospital', 'Apollo Hospital', 'Patient A', 'Patient B',
  'Patient C', 'Traffic Junction', 'Highway', 'Ambulance Depot',
];
const POS_AMBULANCE: [number, number][] = [
  [350, 150], [550, 100], [200, 300], [450, 400],
  [600, 350], [300, 200], [500, 250], [100, 100],
];

export const SAMPLE_DATASETS: Record<string, { name: string; description: string; nodes: GraphNode[]; edges: GraphEdge[] }> = {};

function buildChandigarh() {
  const nodes = makeNodes(NODES_CHANDIGARH, POS_CHANDIGARH);
  const edges = makeEdges(
    ['Chandigarh', 'Chandigarh', 'Mohali', 'Mohali', 'Panchkula', 'Zirakpur', 'Zirakpur', 'Kharar', 'Kharar', 'Dera Bassi', 'Ambala'],
    ['Mohali', 'Panchkula', 'Zirakpur', 'Kharar', 'Ambala', 'Dera Bassi', 'Chandigarh', 'Chandigarh', 'Patiala', 'Patiala', 'Panchkula'],
    [12, 8, 15, 10, 25, 18, 14, 20, 30, 22, 35],
    ['normal', 'normal', 'normal', 'normal', 'normal', 'normal', 'normal', 'normal', 'normal', 'normal', 'normal'],
    nodes,
  );
  SAMPLE_DATASETS.chandigarh = { name: 'Chandigarh Road Network', description: 'Major roads connecting Chandigarh, Mohali, Panchkula, and nearby cities', nodes, edges };
}

function buildCampus() {
  const nodes = makeNodes(NODES_CAMPUS, POS_CAMPUS);
  const edges = makeEdges(
    ['Main Gate', 'Main Gate', 'Admin Block', 'Admin Block', 'Library', 'Library', 'Science Block', 'Science Block', 'Cafeteria', 'Cafeteria', 'Hostel'],
    ['Admin Block', 'Cafeteria', 'Library', 'Science Block', 'Sports Complex', 'Auditorium', 'Admin Block', 'Hostel', 'Main Gate', 'Sports Complex', 'Science Block'],
    [5, 4, 8, 6, 7, 12, 3, 9, 6, 15, 10],
    ['normal', 'normal', 'normal', 'normal', 'normal', 'normal', 'normal', 'normal', 'normal', 'normal', 'normal'],
    nodes,
  );
  SAMPLE_DATASETS.campus = { name: 'University Campus Navigation', description: 'Walking paths across a university campus', nodes, edges };
}

function buildAmbulance() {
  const nodes = makeNodes(NODES_AMBULANCE, POS_AMBULANCE);
  const edges = makeEdges(
    ['City Hospital', 'City Hospital', 'Apollo Hospital', 'Apollo Hospital', 'Patient A', 'Patient A', 'Patient B', 'Patient C', 'Traffic Junction', 'Traffic Junction', 'Highway'],
    ['Traffic Junction', 'Ambulance Depot', 'Highway', 'Ambulance Depot', 'Traffic Junction', 'Patient B', 'Patient C', 'Highway', 'City Hospital', 'Highway', 'Apollo Hospital'],
    [8, 20, 10, 15, 5, 12, 3, 9, 7, 6, 18],
    ['normal', 'normal', 'normal', 'moderate', 'normal', 'heavy', 'blocked', 'normal', 'heavy', 'moderate', 'normal'],
    nodes,
  );
  SAMPLE_DATASETS.ambulance = { name: 'Ambulance Routing Network', description: 'Emergency vehicle routing with hospitals and patient locations', nodes, edges };
}

buildChandigarh();
buildCampus();
buildAmbulance();

export function resolveSampleData(key: string) {
  const ds = SAMPLE_DATASETS[key];
  if (!ds) {
    console.error(`[resolveSampleData] Unknown dataset key: ${key}`);
    return null;
  }
  console.info(`[resolveSampleData] Loaded "${ds.name}" - ${ds.nodes.length} nodes, ${ds.edges.length} edges`);
  return { nodes: ds.nodes, edges: ds.edges };
}
