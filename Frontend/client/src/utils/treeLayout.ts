import type { Node, Edge } from 'reactflow';
import type { TaskDto } from '@/types';

export const buildTreeLayout = (tasks: TaskDto[]) => {
    const nodes: Node[] = [];
    const edges: Edge[] = [];

    // Крок 1: Топологічне сортування та визначення рівнів
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

    // Крок 2: Групуємо завдання по рівнях
    const tasksByLevel: TaskDto[][] = [];
    tasks.forEach(task => {
        const level = taskLevels.get(task.id);
        if (level === undefined) return;

        if (!tasksByLevel[level]) {
            tasksByLevel[level] = [];
        }
        tasksByLevel[level].push(task);
    });

    // Крок 3: Формуємо Вузли (Nodes) із "шумом"
    const CELL_WIDTH = 280;
    const LEVEL_HEIGHT = 160;

    tasksByLevel.forEach((levelTasks, levelIndex) => {
        const count = levelTasks.length;

        levelTasks.forEach((task, index) => {
            const idealX = (index - (count - 1) / 2) * CELL_WIDTH;
            const idealY = levelIndex * -LEVEL_HEIGHT;

            // Детермінований шум: від -20 до +20 px
            const jitterX = ((task.id * 137) % 40) - 20;
            const jitterY = ((task.id * 93) % 40) - 20;

            nodes.push({
                id: task.id.toString(),
                position: {
                    x: idealX + jitterX,
                    y: idealY + jitterY
                },
                type: 'customTaskNode', // Назва нашого кастомного UI-компонента
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

    // Крок 4: Формуємо Ребра (Edges)
    tasks.forEach(task => {
        const prereqs = task.prerequisiteTaskIds || [];
        prereqs.forEach(reqId => {
            const isTaskActive = !task.isLocked && !task.isCompleted;

            edges.push({
                id: `e${reqId}-${task.id}`,
                source: reqId.toString(),
                target: task.id.toString(),
                type: 'default', // Звичайна плавна лінія
                animated: isTaskActive, // Анімуємо лінію, якщо завдання зараз актуальне
                style: {
                    stroke: isTaskActive ? '#a855f7' : '#3f3f46', // Фіолетова для активних, сіра для інших
                    strokeWidth: 3
                },
            });
        });
    });

    return { nodes, edges };
};