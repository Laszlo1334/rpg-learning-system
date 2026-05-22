import { Handle, Position } from 'reactflow';
import { Lock, Sparkles, Skull, Flame } from 'lucide-react';

export const CustomTaskNode = ({ data }: any) => {
    const isBoss = data.type === 'BOSS';

    let nodeBg = "bg-zinc-800 border-zinc-700 text-zinc-400";
    let fogOfWarClass = "";

    if (data.isCompleted) {
        nodeBg = "bg-green-500/20 border-green-500 text-green-400 shadow-[0_0_15px_rgba(34,197,94,0.3)]";
    } else if (data.isLocked) {
        // Fog of War: blur and dim
        nodeBg = "bg-zinc-950/40 border-zinc-800/30 text-zinc-700 cursor-not-allowed";
        fogOfWarClass = "backdrop-blur-sm grayscale opacity-60";
    } else if (isBoss) {
        nodeBg = "bg-red-950 border-red-500 text-red-500 shadow-[0_0_20px_rgba(220,38,38,0.4)] animate-pulse";
    } else {
        nodeBg = "bg-zinc-900 border-purple-500 text-purple-400 shadow-[0_0_15px_rgba(168,85,247,0.3)]";
    }

    return (
        <div className="relative group flex items-center justify-center">
            <Handle type="target" position={Position.Bottom} className="w-1 h-1 bg-transparent border-0" />
            <Handle type="source" position={Position.Top} className="w-1 h-1 bg-transparent border-0" />

            <div
                className={`w-14 h-14 rounded-full border-2 flex items-center justify-center transition-transform hover:scale-110 z-10 ${nodeBg} ${fogOfWarClass} ${!data.isLocked ? 'cursor-pointer' : ''}`}
            >
                {data.isLocked ? <Lock size={20} /> : data.isCompleted ? <Sparkles size={20} /> : isBoss ? <Skull size={20} /> : <Flame size={20} />}
            </div>

            {/* Tooltip: appears on hover, shows task title and status */}
            <div className="absolute left-full ml-4 top-1/2 -translate-y-1/2 w-64 p-4 rounded-xl bg-zinc-900 border border-zinc-700 shadow-2xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-300 delay-500 z-50 pointer-events-none">
                <div className="flex flex-col gap-2">
                    <h3 className="font-bold text-white text-sm">{data.title}</h3>

                    <div className="text-xs font-bold mt-1">
                        {data.isLocked ? (
                            <span className="text-zinc-500 flex items-center gap-1"><Lock size={12} /> Locked</span>
                        ) : data.isCompleted ? (
                            <span className="text-green-400 flex items-center gap-1"><Sparkles size={12} /> Completed</span>
                        ) : (
                            <span className="text-purple-400 flex items-center gap-1"><Flame size={12} /> Available</span>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};