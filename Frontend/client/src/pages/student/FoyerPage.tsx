import { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ReactFlow, { Background, Controls } from 'reactflow';
import 'reactflow/dist/style.css';

import { CustomTaskNode } from '@/components/CustomTaskNode';
import { buildTreeLayout } from '@/utils/treeLayout';
import { taskService } from '@/services/taskService';
import type { TaskDto } from '@/types';
import { ArrowLeft } from 'lucide-react';

// Виносимо за межі компонента для оптимізації
const nodeTypes = {
    customTaskNode: CustomTaskNode,
};

export const FoyerPage = () => {
    // Отримуємо ID курсу з URL (наприклад, /course/1/foyer)
    const { courseId } = useParams<{ courseId: string }>();
    const navigate = useNavigate();

    const [tasks, setTasks] = useState<TaskDto[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchTasks = async () => {
            try {
                if (courseId) {
                    const data = await taskService.getTasksByCourse(Number(courseId));
                    setTasks(data);
                }
            } catch (error) {
                console.error("Помилка завантаження карти:", error);
            } finally {
                setIsLoading(false);
            }
        };
        fetchTasks();
    }, [courseId]);

    // Перебудовуємо дерево тільки при зміні списку завдань
    const { nodes, edges } = useMemo(() => buildTreeLayout(tasks), [tasks]);

    if (isLoading) {
        return (
            <div className="min-h-screen bg-zinc-950 flex items-center justify-center text-zinc-500 font-bold">
                Формування мапи...
            </div>
        );
    }

    return (
        <div className="w-screen h-screen bg-zinc-950 relative">
            <button
                onClick={() => navigate('/dashboard')}
                className="absolute top-4 left-4 z-10 flex items-center gap-2 text-zinc-400 hover:text-white transition-colors font-bold bg-zinc-900/80 px-4 py-2 rounded-xl backdrop-blur-sm border border-zinc-800"
            >
                <ArrowLeft size={20} /> До списку курсів
            </button>

            <ReactFlow
                nodes={nodes}
                edges={edges}
                nodeTypes={nodeTypes}
                fitView
                nodesDraggable={false}
                nodesConnectable={false}
                elementsSelectable={false}
                proOptions={{ hideAttribution: true }}
            >
                <Background color="#27272a" gap={24} />
                <Controls showInteractive={false} className="bg-zinc-900 border-zinc-800 fill-white" />
            </ReactFlow>
        </div>
    );
};