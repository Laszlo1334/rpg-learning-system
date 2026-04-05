import { Handle, Position } from 'reactflow';
import { Lock, Sparkles, Skull, Flame } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const CustomTaskNode = ({ data }: any) => {
    const navigate = useNavigate();
    const isBoss = data.type === 'BOSS';

    const handleNodeClick = () => {
        if (!data.isLocked) {
            // Переходимо на Арену, якщо рівень відкритий
            navigate(`/arena/${data.id}`);
        }
    };

    // Базові стилі залежно від статусу
    let bgClass = "bg-zinc-900 border-zinc-800 text-zinc-300";
    if (data.isCompleted) bgClass = "bg-green-950/30 border-green-500/50 text-green-400 shadow-[0_0_15px_rgba(34,197,94,0.2)]";
    else if (data.isLocked) bgClass = "bg-zinc-950 border-zinc-800/50 text-zinc-600 opacity-80 cursor-not-allowed";
    else if (isBoss) bgClass = "bg-zinc-950 border-red-900/50 text-red-500 shadow-[0_0_20px_rgba(220,38,38,0.3)] animate-pulse";
    else bgClass = "bg-zinc-900 border-purple-500/50 text-white shadow-[0_0_15px_rgba(168,85,247,0.2)] hover:-translate-y-1";

    return (
        <div
            onClick={handleNodeClick}
            className={`w-64 p-4 rounded-2xl border-2 transition-all duration-300 relative ${bgClass} ${!data.isLocked ? 'cursor-pointer hover:border-white' : ''}`}
        >
            <Handle type="target" position={Position.Bottom} className="w-3 h-3 bg-zinc-600 border-2 border-zinc-950 opacity-0 group-hover:opacity-100 transition-opacity" />
            <Handle type="source" position={Position.Top} className="w-3 h-3 bg-zinc-600 border-2 border-zinc-950 opacity-0 group-hover:opacity-100 transition-opacity" />

            <div className="flex items-center gap-3">
                <div className={`p-2 rounded-xl ${data.isCompleted ? 'bg-green-500/20' : isBoss ? 'bg-red-950' : 'bg-zinc-800'}`}>
                    {data.isLocked ? <Lock size={20} /> : data.isCompleted ? <Sparkles size={20} /> : isBoss ? <Skull size={20} /> : <Flame size={20} className={isBoss ? "text-red-500" : "text-purple-400"} />}
                </div>
                <div>
                    <h3 className="font-bold text-sm line-clamp-2">{data.title}</h3>
                    {isBoss && <p className="text-xs text-red-400 mt-1 opacity-80">{data.bossMetadata?.bossName}</p>}
                </div>
            </div>
        </div>
    );
};