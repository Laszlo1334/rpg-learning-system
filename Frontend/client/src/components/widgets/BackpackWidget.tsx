// src/components/widgets/BackpackWidget.tsx
import { Loader2, Backpack, Zap } from 'lucide-react';
import type { InventoryEntry } from '@/types';

interface BackpackWidgetProps {
    inventory: InventoryEntry[];
    onUseItem: (inventoryId: number, itemName: string, itemDescription: string, assetUrl: string | null) => Promise<void>;
    isLoading?: boolean;
}

// Map effect keys → short emoji label for compact display
const EFFECT_EMOJI: Record<string, string> = {
    XP_BOOST_30_MIN:      '🧪 XP ×1.5',
    GOLD_BOOST_60_MIN:    '🧲 Gold ×2',
    ENERGY_STASIS_30_MIN: '☕ Енергія',
    SINGLE_RUN_SHIELD:    '🛡️ Щит',
    NONE:                 '',
};

export const BackpackWidget = ({ inventory, onUseItem, isLoading = false }: BackpackWidgetProps) => {
    // Filter: consumables only, quantity > 0
    const consumables = inventory.filter(
        entry => entry.item.category === 'CONSUMABLE' && entry.quantity > 0
    );

    return (
        <div className="h-full bg-zinc-900 border border-zinc-800 rounded-2xl p-5 flex flex-col min-h-[160px] transition-all duration-300 hover:border-amber-500/30 hover:shadow-[0_0_20px_rgba(251,191,36,0.07)]">

            {/* Header */}
            <div className="flex items-center gap-2 mb-4">
                <Backpack size={20} className="text-amber-400" />
                <span className="font-black text-white text-sm">Рюкзак</span>
                {consumables.length > 0 && (
                    <span className="ml-auto text-[10px] font-black text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2 py-0.5 rounded-full">
                        {consumables.length} тип{consumables.length > 1 ? 'и' : ''}
                    </span>
                )}
            </div>

            {/* Body */}
            {isLoading ? (
                <div className="flex-1 flex items-center justify-center text-zinc-600">
                    <Loader2 size={20} className="animate-spin" />
                </div>
            ) : consumables.length === 0 ? (
                <div className="flex-1 flex flex-col items-center justify-center text-center gap-2 py-2">
                    <span className="text-3xl opacity-30">🎒</span>
                    <p className="text-zinc-600 text-xs font-bold leading-snug max-w-[160px]">
                        Ваш рюкзак порожній.<br />Відвідайте Крамницю Гільдії.
                    </p>
                </div>
            ) : (
                <ul className="flex flex-col gap-2 overflow-y-auto flex-1">
                    {consumables.map(entry => (
                        <li
                            key={entry.id}
                            className="flex items-center gap-3 bg-zinc-950 border border-zinc-800 rounded-xl px-3 py-2.5 group hover:border-amber-500/30 transition-all duration-200"
                        >
                            {/* Icon */}
                            {entry.item.assetUrl ? (
                                <img
                                    src={entry.item.assetUrl}
                                    alt={entry.item.name}
                                    className="w-8 h-8 object-contain flex-shrink-0"
                                    style={{ imageRendering: 'pixelated' }}
                                    onError={(e) => { e.currentTarget.style.display = 'none'; }}
                                />
                            ) : (
                                <span className="text-xl flex-shrink-0">🧪</span>
                            )}

                            {/* Info */}
                            <div className="flex-1 min-w-0">
                                <p className="text-xs font-bold text-white truncate leading-tight">
                                    {entry.item.name}
                                </p>
                                {EFFECT_EMOJI[entry.item.effect] && (
                                    <p className="text-[10px] text-zinc-500 mt-0.5 truncate">
                                        {EFFECT_EMOJI[entry.item.effect]}
                                    </p>
                                )}
                            </div>

                            {/* Quantity badge */}
                            <span className="text-xs font-black text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2 py-0.5 rounded-lg flex-shrink-0">
                                ×{entry.quantity}
                            </span>

                            <button
                                onClick={() => onUseItem(entry.id, entry.item.name, entry.item.description, entry.item.assetUrl)}
                                className="flex-shrink-0 flex items-center gap-1 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/20 hover:border-amber-500/40 text-amber-400 rounded-lg px-2.5 py-1.5 text-[11px] font-black transition-all duration-200 opacity-0 group-hover:opacity-100"
                                title="Застосувати"
                            >
                                <Zap size={12} />
                                Вжити
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};
