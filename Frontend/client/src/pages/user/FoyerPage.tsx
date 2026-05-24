import { useState, useEffect, useMemo, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ReactFlow, { Background, PanOnScrollMode } from 'reactflow';
import 'reactflow/dist/style.css';

import { CustomTaskNode } from '@/components/CustomTaskNode';
import { buildTreeLayout } from '@/utils/treeLayout';
import { taskService } from '@/services/taskService';
import { useAuthStore } from '@/store/authStore';
import type { TaskDto } from '@/types';
import { ArrowLeft, Zap } from 'lucide-react';
import { useTheme } from '@/utils/useTheme';

const nodeTypes = {
    customTaskNode: CustomTaskNode,
};

export const FoyerPage = () => {
    const { courseId } = useParams<{ courseId: string }>();
    const navigate = useNavigate();
    const energy = useAuthStore(state => state.user?.energy ?? 0);
    const { theme } = useTheme();
    const isDarkMode = theme === 'dark';

    const [tasks, setTasks] = useState<TaskDto[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    const [selectedTask, setSelectedTask] = useState<TaskDto | null>(null);
    const hasEnergy = energy > 0;

    useEffect(() => {
        const fetchTasks = async () => {
            try {
                if (courseId) {
                    localStorage.setItem('lastActiveCourseId', courseId);
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

    const { nodes, edges } = useMemo(() => buildTreeLayout(tasks), [tasks]);

    const onNodeClick = useCallback((event: React.MouseEvent, node: any) => {
        if (!node.data.isLocked) {
            setSelectedTask(node.data);
        }
    }, []);

    if (isLoading) {
        return (
            <div className="min-h-screen bg-[#FBF7F0] dark:bg-zinc-950 flex items-center justify-center text-[#8C7A65] dark:text-zinc-500 font-bold">
                Формування мапи...
            </div>
        );
    }

    const activeNode = nodes.find(n => !n.data.isLocked && !n.data.isCompleted);
    const focusNodeId = activeNode ? activeNode.id : (nodes.length > 0 ? nodes[nodes.length - 1].id : undefined);

    return (
        <div className="w-full h-screen bg-[#FBF7F0] dark:bg-zinc-950 relative overflow-hidden">
            {/* ReactFlow CSS overrides — required to suppress default cursor/pointer styles */}
            <style>{`
                /* Remove grab cursor on the background pane */
                .react-flow__pane {
                    cursor: default !important;
                }
                /* Disable pointer events on edges so they don't intercept clicks */
                .react-flow__edge, .react-flow__edge-path, .react-flow__edge-interaction {
                    pointer-events: none !important;
                    cursor: default !important;
                }
                /* Hide connection handles on nodes */
                .react-flow__handle {
                    opacity: 0 !important;
                    pointer-events: none !important;
                }
            `}</style>

            <button
                onClick={() => navigate('/courses')}
            className="absolute top-4 left-4 z-10 flex items-center gap-2 text-[#8C7A65] dark:text-zinc-400 hover:text-[#4A3B2F] dark:hover:text-white transition-colors font-bold bg-[#F6F1E6]/90 dark:bg-zinc-900/80 px-4 py-2 rounded-xl backdrop-blur-sm border border-[#D6CAB4] dark:border-zinc-800"
            >
                <ArrowLeft size={20} /> До списку курсів
            </button>

            <ReactFlow
                nodes={nodes}
                edges={edges}
                nodeTypes={nodeTypes}
                onNodeClick={onNodeClick}
                fitView
                fitViewOptions={{
                    nodes: focusNodeId ? [{ id: focusNodeId }] : undefined,
                    maxZoom: 1.2,
                    minZoom: 0.5,
                    duration: 800
                }}
                defaultEdgeOptions={{ interactionWidth: 0, focusable: false }}
                edgesFocusable={false}
                edgesUpdatable={false}
                nodesDraggable={false}
                nodesConnectable={false}
                elementsSelectable={true}
                panOnDrag={false}
                zoomOnScroll={false}
                zoomOnDoubleClick={false}
                panOnScroll={true}
                panOnScrollMode={PanOnScrollMode.Vertical}
                proOptions={{ hideAttribution: true }}
            >
                <Background color={isDarkMode ? "#27272a" : "#D6CAB4"} gap={24} />
            </ReactFlow>

            {selectedTask && (
                <div className="absolute inset-0 z-50 flex items-center justify-center bg-[#4A3B2F]/30 dark:bg-black/60 backdrop-blur-sm">
                    <div className="bg-[#F6F1E6] dark:bg-zinc-900 border border-[#D6CAB4] dark:border-zinc-700 p-8 rounded-2xl max-w-md w-full text-center shadow-2xl">
                        <h2 className="text-2xl font-bold text-[#4A3B2F] dark:text-white mb-4">{selectedTask.title}</h2>
                        <p className="text-[#8C7A65] dark:text-zinc-400 mb-8">Ви готові розпочати це випробування?</p>

                    <div className="flex justify-center gap-4">
                            <button
                                onClick={() => setSelectedTask(null)}
                                className="px-6 py-2 rounded-xl font-bold text-[#8C7A65] dark:text-zinc-400 hover:text-[#4A3B2F] dark:hover:text-white hover:bg-[#EDE6D6] dark:hover:bg-zinc-800 transition-colors"
                            >
                                Відступити
                            </button>
                            <div className="flex flex-col items-center gap-3 w-full">
                                <button onClick={() => navigate(`/arena/${selectedTask.id}`)}
                                    className="w-full px-8 py-3 rounded-xl font-bold text-white bg-purple-600 hover:bg-purple-500 shadow-[0_0_15px_rgba(168,85,247,0.4)] transition-all">
                                    До бою!
                                </button>
                                <span className={`text-xs font-bold flex items-center gap-1 ${hasEnergy ? 'text-green-400' : 'text-[#8C7A65] dark:text-zinc-500'}`}>
                                    {hasEnergy ? <><Zap size={14}/> Бонус до нагород: x1.5</> : 'Втома: стандартні нагороди'}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};