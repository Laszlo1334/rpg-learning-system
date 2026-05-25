import type { Node, Edge } from 'reactflow';
import type { TaskDto } from '@/types';
import dagre from 'dagre';

// Returns the correct inactive edge colour based on the active theme.
// Reads the <html> class at call time (inside useMemo, so it stays in sync).
const inactiveEdgeStroke = () =>
    document.documentElement.classList.contains('dark') ? '#3f3f46' : '#C4B49A';

const getDeterministicNoise = (id: string | number, seed = 1) => {
    const str = String(id);
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = Math.imul(31, hash) + str.charCodeAt(i) | 0;
    }
    const random = Math.sin(hash * seed) * 10000;
    return random - Math.floor(random);
};

export const buildTreeLayout = (tasks: TaskDto[]) => {
    // Generate ReactFlow nodes with dummy positions
    const nodes: Node[] = tasks.map(task => ({
        id: task.id.toString(),
        type: 'customTaskNode',
        data: {
            id: task.id,
            title: task.title,
            type: task.type,
            bossMetadata: task.bossMetadata,
            isLocked: task.isLocked,
            isCompleted: task.isCompleted
        },
        position: { x: 0, y: 0 }
    }));

    // Build edges from prerequisite relationships
    const edges: Edge[] = [];
    tasks.forEach(task => {
        const prereqs = task.prerequisiteTaskIds || [];
        prereqs.forEach(reqId => {
            const isTaskActive = !task.isLocked && !task.isCompleted;

            edges.push({
                id: `e${reqId}-${task.id}`,
                source: reqId.toString(),
                target: task.id.toString(),
                type: 'default',
                animated: isTaskActive, // Animate edge when the dependent task is currently actionable
                style: {
                    stroke: isTaskActive ? '#a855f7' : inactiveEdgeStroke(),
                    strokeWidth: 3
                },
            });
        });
    });

    // Create a new dagre graph layout engine
    const dagreGraph = new dagre.graphlib.Graph();
    dagreGraph.setDefaultEdgeLabel(() => ({}));

    // Configure layout direction to be Bottom-to-Top (BT),
    // along with optimal horizontal and vertical spacing.
    dagreGraph.setGraph({
        rankdir: 'BT',
        nodesep: 80,
        ranksep: 120
    });

    // Feed nodes with CustomTaskNode's approximate dimensions
    nodes.forEach(node => {
        dagreGraph.setNode(node.id, { width: 100, height: 100 });
    });

    // Feed edges into the graph
    edges.forEach(edge => {
        dagreGraph.setEdge(edge.source, edge.target);
    });

    // Compute layout
    dagre.layout(dagreGraph);

    // Apply calculated node positions back, offsetting by half width/height to center them and adding organic noise
    const positionedNodes = nodes.map(node => {
        const nodeWithPosition = dagreGraph.node(node.id);
        const MAX_NOISE = 55;
        const xNoise = (getDeterministicNoise(node.id, 123) * 2 - 1) * MAX_NOISE;
        const yNoise = (getDeterministicNoise(node.id, 456) * 2 - 1) * MAX_NOISE;

        return {
            ...node,
            position: {
                x: nodeWithPosition.x - 50 + xNoise,
                y: nodeWithPosition.y - 50 + yNoise
            }
        };
    });

    return { nodes: positionedNodes, edges };
};