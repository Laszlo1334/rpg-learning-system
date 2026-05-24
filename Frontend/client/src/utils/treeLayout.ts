import type { Node, Edge } from 'reactflow';
import type { TaskDto } from '@/types';

// Returns the correct inactive edge colour based on the active theme.
// Reads the <html> class at call time (inside useMemo, so it stays in sync).
const inactiveEdgeStroke = () =>
    document.documentElement.classList.contains('dark') ? '#3f3f46' : '#C4B49A';

export const buildTreeLayout = (tasks: TaskDto[]) => {
    const nodes: Node[] = [];
    const edges: Edge[] = [];

    // Step 1: Topological sort — assign a depth level to each task
    const taskLevels = new Map<number, number>();
    let remainingTasks = [...tasks];

    while (remainingTasks.length > 0) {
        const initialLength = remainingTasks.length;

        remainingTasks = remainingTasks.filter(task => {
            const prereqs = task.prerequisiteTaskIds || [];
            const allPrereqsCalculated = prereqs.every(id => taskLevels.has(id));

            if (allPrereqsCalculated) {
                let maxPrereqLevel = -1;
                for (const reqId of prereqs) {
                    const reqLevel = taskLevels.get(reqId)!;
                    if (reqLevel > maxPrereqLevel) {
                        maxPrereqLevel = reqLevel;
                    }
                }
                taskLevels.set(task.id, maxPrereqLevel + 1);
                return false;
            }
            return true;
        });

        if (remainingTasks.length === initialLength) {
            console.error("Виявлено циклічну залежність у завданнях!");
            break;
        }
    }

    // Step 2: Group tasks by depth level
    const tasksByLevel: TaskDto[][] = [];
    tasks.forEach(task => {
        const level = taskLevels.get(task.id);
        if (level === undefined) return;

        if (!tasksByLevel[level]) {
            tasksByLevel[level] = [];
        }
        tasksByLevel[level].push(task);
    });

    // Step 3: Build ReactFlow nodes with deterministic position jitter
    const CELL_WIDTH = 280;
    const LEVEL_HEIGHT = 160;

    tasksByLevel.forEach((levelTasks, levelIndex) => {
        const count = levelTasks.length;

        levelTasks.forEach((task, index) => {
            const idealX = (index - (count - 1) / 2) * CELL_WIDTH;
            const idealY = levelIndex * -LEVEL_HEIGHT;

            // Deterministic per-task jitter: spreads overlapping nodes without randomness
            const jitterX = ((task.id * 137) % 40) - 20;
            const jitterY = ((task.id * 93) % 40) - 20;

            nodes.push({
                id: task.id.toString(),
                position: {
                    x: idealX + jitterX,
                    y: idealY + jitterY
                },
                type: 'customTaskNode',
                data: {
                    id: task.id,
                    title: task.title,
                    type: task.type,
                    bossMetadata: task.bossMetadata,
                    isLocked: task.isLocked,
                    isCompleted: task.isCompleted
                }
            });
        });
    });

    // Step 4: Build edges from prerequisite relationships
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

    return { nodes, edges };
};