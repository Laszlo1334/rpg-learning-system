// src/pages/ShopPage.tsx
import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { shopService } from '@/services/shopService';
import { inventoryService } from '@/services/inventoryService';
import type { Item, InventoryEntry } from '@/types';
import {
    ArrowLeft, Coins, Gem, ShoppingCart, Loader2,
    Shirt, FlaskConical, AlertCircle, X, Eye, CheckCircle
} from 'lucide-react';

type ActiveTab = 'COSMETIC' | 'CONSUMABLE';

// Backend effect → readable label
const EFFECT_LABELS: Record<string, string> = {
    XP_BOOST_30_MIN:      '🧪 XP ×1.5 на 30 хв',
    GOLD_BOOST_60_MIN:    '🧲 Gold ×2 на 60 хв',
    ENERGY_STASIS_30_MIN: '☕ Енергія не витрачається 30 хв',
    SINGLE_RUN_SHIELD:    '🛡️ Захист на 1 забіг',
    NONE:                 '',
};

export const ShopPage = () => {
    const navigate = useNavigate();
    const { user, refreshUser } = useAuthStore();

    // ─── Data state ────────────────────────────────────────────────
    const [items, setItems] = useState<Item[]>([]);
    const [inventory, setInventory] = useState<InventoryEntry[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    // ─── UI state ──────────────────────────────────────────────────
    const [activeTab, setActiveTab] = useState<ActiveTab>('CONSUMABLE');
    const [buyingId, setBuyingId] = useState<number | null>(null);
    const [itemToBuy, setItemToBuy] = useState<Item | null>(null);        // confirmation modal
    const [previewItem, setPreviewItem] = useState<Item | null>(null);    // cosmetic preview
    const [notification, setNotification] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

    // ─── Load items + inventory on mount ───────────────────────────
    useEffect(() => {
        const load = async () => {
            try {
                const [shopData, inventoryData] = await Promise.all([
                    shopService.getItems(),
                    inventoryService.getInventory(),
                ]);
                setItems(shopData);
                setInventory(inventoryData);
            } catch (err) {
                console.error('[ShopPage] Load error:', err);
            } finally {
                setIsLoading(false);
            }
        };
        load();
    }, []);

    // ─── Auto-dismiss notification after 3 s ───────────────────────
    useEffect(() => {
        if (!notification) return;
        const timer = setTimeout(() => setNotification(null), 3000);
        return () => clearTimeout(timer);
    }, [notification]);

    // ─── Purchase (called from confirm modal) ──────────────────────
    const handleConfirmBuy = useCallback(async () => {
        if (!itemToBuy) return;
        setBuyingId(itemToBuy.id);
        const boughtItem = itemToBuy;
        setItemToBuy(null);

        try {
            await shopService.buyItem(boughtItem.id);

            // Refresh both balance and inventory in parallel
            const [, newInventory] = await Promise.all([
                refreshUser(),
                inventoryService.getInventory(),
            ]);
            setInventory(newInventory);

            setNotification({ type: 'success', message: `"${boughtItem.name}" додано до рюкзака! ✅` });
        } catch (err: unknown) {
            const raw = err instanceof Error ? err.message : '';
            setNotification({ type: 'error', message: raw || 'Помилка при покупці' });
        } finally {
            setBuyingId(null);
        }
    }, [itemToBuy, refreshUser]);

    // ─── Helpers ───────────────────────────────────────────────────
    const canAfford = (item: Item): boolean => {
        if (!user) return false;
        return item.currencyType === 'GOLD' ? user.gold >= item.price : user.crystals >= item.price;
    };

    const getOwnedQuantity = (item: Item): number => {
        const entry = inventory.find(inv => inv.item.id === item.id);
        return entry ? entry.quantity : 0;
    };

    const filteredItems = items.filter(i => i.category === activeTab);

    const tabClass = (tab: ActiveTab) =>
        `flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl font-bold text-sm transition-all ${
            activeTab === tab ? 'bg-zinc-800 text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-300'
        }`;

    // ─── Shared item card (consumables grid + cosmetics right col) ──
    const ItemCard = ({ item, onCardClick }: { item: Item; onCardClick?: () => void }) => {
        const affordable = canAfford(item);
        const isBuying = buyingId === item.id;
        const ownedQty  = getOwnedQuantity(item);
        const isPreview = previewItem?.id === item.id;

        return (
            <div
                onClick={onCardClick}
                className={`bg-zinc-900 border rounded-2xl p-5 flex flex-col gap-3 transition-all duration-200 ${
                    isPreview
                        ? 'border-purple-500/60 shadow-[0_0_20px_rgba(168,85,247,0.15)]'
                        : affordable
                            ? 'border-zinc-800 hover:border-zinc-600 hover:shadow-lg'
                            : 'border-zinc-800/50 opacity-60'
                } ${onCardClick ? 'cursor-pointer' : ''}`}
            >
                {/* Icon */}
                <div className="w-12 h-12 rounded-xl bg-zinc-800 border border-zinc-700 flex items-center justify-center text-2xl">
                    {item.category === 'COSMETIC' ? '✨' : '🧪'}
                </div>

                {/* Info */}
                <div className="flex-1">
                    <h3 className="font-black text-white text-base leading-tight">{item.name}</h3>
                    <p className="text-zinc-500 text-xs mt-1 leading-relaxed">{item.description}</p>
                    {EFFECT_LABELS[item.effect] && (
                        <p className="text-xs text-blue-400 mt-2 font-bold">{EFFECT_LABELS[item.effect]}</p>
                    )}
                </div>

                {/* Owned quantity (consumables only) */}
                {item.category === 'CONSUMABLE' && (
                    <p className="text-xs text-zinc-500">
                        В наявності: <span className={ownedQty > 0 ? 'text-green-400 font-bold' : ''}>{ownedQty} шт.</span>
                    </p>
                )}

                {/* Price + Buy */}
                <div className="flex items-center justify-between gap-2 mt-auto pt-3 border-t border-zinc-800">
                    <div className="flex items-center gap-1.5 font-black text-base">
                        {item.currencyType === 'GOLD'
                            ? <Coins size={14} className="text-yellow-400" />
                            : <Gem   size={14} className="text-purple-400" />
                        }
                        <span className={item.currencyType === 'GOLD' ? 'text-yellow-400' : 'text-purple-400'}>
                            {item.price}
                        </span>
                    </div>

                    {onCardClick && (
                        <button
                            onClick={e => { e.stopPropagation(); setPreviewItem(isPreview ? null : item); }}
                            className="flex items-center gap-1 px-2 py-1.5 rounded-lg text-zinc-400 hover:text-purple-400 hover:bg-purple-500/10 transition-colors text-xs font-bold"
                        >
                            <Eye size={14} /> Приміряти
                        </button>
                    )}

                    <button
                        onClick={e => { e.stopPropagation(); if (affordable && !isBuying) setItemToBuy(item); }}
                        disabled={!affordable || isBuying}
                        className={`flex items-center gap-1.5 px-4 py-2 rounded-xl font-bold text-sm transition-all disabled:cursor-not-allowed ${
                            affordable ? 'bg-blue-600 hover:bg-blue-500 text-white' : 'bg-zinc-800 text-zinc-500'
                        }`}
                    >
                        {isBuying
                            ? <Loader2 size={16} className="animate-spin" />
                            : <ShoppingCart size={16} />
                        }
                        {isBuying ? 'Купую...' : affordable ? 'Купити' : 'Мало коштів'}
                    </button>
                </div>
            </div>
        );
    };

    // ──────────────────────────────────────────────────────────────────
    return (
        <div className="p-6 max-w-5xl mx-auto space-y-6">

            {/* ── Toast ─────────────────────────────────────────────── */}
            {notification && (
                <div className={`fixed top-6 left-1/2 -translate-x-1/2 z-50 flex items-center gap-3 px-5 py-3 rounded-2xl shadow-2xl font-bold text-sm ${
                    notification.type === 'success'
                        ? 'bg-emerald-900 border border-emerald-700 text-emerald-200'
                        : 'bg-red-900 border border-red-700 text-red-200'
                }`}>
                    <AlertCircle size={18} />
                    {notification.message}
                </div>
            )}

            {/* ── Confirmation Modal ────────────────────────────────── */}
            {itemToBuy && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm"
                    onClick={() => setItemToBuy(null)}
                >
                    <div
                        className="bg-zinc-900 border border-zinc-800 rounded-2xl p-6 max-w-sm w-full shadow-2xl"
                        onClick={e => e.stopPropagation()}
                    >
                        {/* Header */}
                        <div className="flex items-center justify-between mb-4">
                            <h3 className="text-lg font-black text-white">Підтвердження покупки</h3>
                            <button onClick={() => setItemToBuy(null)} className="text-zinc-500 hover:text-white transition-colors">
                                <X size={20} />
                            </button>
                        </div>

                        {/* Item summary */}
                        <div className="bg-zinc-950 border border-zinc-800 rounded-xl p-4 mb-5 flex items-center gap-3">
                            <span className="text-3xl">{itemToBuy.category === 'COSMETIC' ? '✨' : '🧪'}</span>
                            <div>
                                <p className="font-black text-white">{itemToBuy.name}</p>
                                <p className="text-sm text-zinc-500 flex items-center gap-1 mt-0.5">
                                    Витратити&nbsp;
                                    {itemToBuy.currencyType === 'GOLD'
                                        ? <><Coins size={14} className="text-yellow-400" /><span className="text-yellow-400 font-bold">{itemToBuy.price} золота</span></>
                                        : <><Gem   size={14} className="text-purple-400" /><span className="text-purple-400 font-bold">{itemToBuy.price} кристалів</span></>
                                    }
                                </p>
                            </div>
                        </div>

                        {/* Actions */}
                        <div className="flex gap-3">
                            <button
                                onClick={() => setItemToBuy(null)}
                                className="flex-1 py-2.5 rounded-xl font-bold bg-zinc-800 text-zinc-300 hover:bg-zinc-700 transition-colors"
                            >
                                Скасувати
                            </button>
                            <button
                                onClick={handleConfirmBuy}
                                className="flex-1 py-2.5 rounded-xl font-bold bg-blue-600 hover:bg-blue-500 text-white transition-colors flex items-center justify-center gap-2"
                            >
                                <CheckCircle size={16} /> Підтвердити
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ── Page Header ───────────────────────────────────────── */}
            <div className="flex items-center justify-between gap-4 flex-wrap">
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => navigate('/dashboard')}
                        className="p-2 text-zinc-400 hover:text-white hover:bg-zinc-800 rounded-xl transition-colors"
                        title="Back to Camp"
                    >
                        <ArrowLeft size={22} />
                    </button>
                    <div>
                        <h1 className="text-2xl font-black text-white">Крамниця Гільдії</h1>
                        <p className="text-zinc-500 text-sm">Витрач своє золото та кристали з розумом</p>
                    </div>
                </div>

                <div className="flex items-center gap-3">
                    <div className="flex items-center gap-2 bg-zinc-900 border border-zinc-800 px-4 py-2 rounded-xl">
                        <Coins size={18} className="text-yellow-400" />
                        <span className="font-black text-white">{user?.gold ?? 0}</span>
                    </div>
                    <div className="flex items-center gap-2 bg-zinc-900 border border-zinc-800 px-4 py-2 rounded-xl">
                        <Gem size={18} className="text-purple-400" />
                        <span className="font-black text-white">{user?.crystals ?? 0}</span>
                    </div>
                </div>
            </div>

            {/* ── Tabs ──────────────────────────────────────────────── */}
            <div className="flex bg-zinc-900 border border-zinc-800 rounded-2xl p-1 gap-1">
                <button
                    onClick={() => { setActiveTab('CONSUMABLE'); setPreviewItem(null); }}
                    className={tabClass('CONSUMABLE')}
                >
                    <FlaskConical size={16} /> Розхідники
                </button>
                <button
                    onClick={() => { setActiveTab('COSMETIC'); setPreviewItem(null); }}
                    className={tabClass('COSMETIC')}
                >
                    <Shirt size={16} /> Косметика
                </button>
            </div>

            {/* ── Content ───────────────────────────────────────────── */}
            {isLoading ? (
                <div className="flex items-center justify-center py-20 gap-3 text-zinc-500">
                    <Loader2 size={24} className="animate-spin" />
                    <span className="font-bold">Завантаження товарів...</span>
                </div>
            ) : filteredItems.length === 0 ? (
                <div className="py-20 text-center text-zinc-500 font-bold">Тут поки що пусто 🧹</div>
            ) : activeTab === 'CONSUMABLE' ? (

                /* ── Consumables: simple 3-col grid ──────────────────── */
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    {filteredItems.map(item => <ItemCard key={item.id} item={item} />)}
                </div>

            ) : (

                /* ── Cosmetics: fitting-room layout ──────────────────── */
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

                    {/* LEFT: Fitting room */}
                    <div className="md:col-span-1">
                        <div className="bg-zinc-900 border border-zinc-800 rounded-2xl p-5 sticky top-24">
                            <p className="text-xs font-black text-zinc-500 uppercase tracking-widest mb-4">
                                ✨ Примірочна
                            </p>

                            {/* Composite avatar */}
                            <div className="relative w-48 h-48 mx-auto">
                                {/* Base avatar */}
                                <div className="absolute inset-0 bg-zinc-800 rounded-2xl border border-zinc-700 flex items-center justify-center text-7xl overflow-hidden">
                                    {user?.avatarUrl
                                        ? <img src={user.avatarUrl} alt="Avatar" className="w-full h-full object-cover" />
                                        : '🧙‍♂️'
                                    }
                                </div>

                                {/* Cosmetic overlay (frame / background) */}
                                {previewItem?.assetUrl && (
                                    <img
                                        src={previewItem.assetUrl}
                                        alt={previewItem.name}
                                        className="absolute inset-0 w-full h-full object-cover rounded-2xl pointer-events-none"
                                        style={{ zIndex: 10 }}
                                    />
                                )}

                                {/* "Worn" badge */}
                                {previewItem && (
                                    <div className="absolute -bottom-3 left-1/2 -translate-x-1/2 bg-purple-600 text-white text-[10px] font-black px-3 py-1 rounded-full whitespace-nowrap shadow-lg">
                                        {previewItem.name}
                                    </div>
                                )}
                            </div>

                            {!previewItem && (
                                <p className="text-center text-xs text-zinc-600 mt-6 font-bold">
                                    Клікни на товар, щоб приміряти
                                </p>
                            )}

                            {previewItem && (
                                <button
                                    onClick={() => setPreviewItem(null)}
                                    className="mt-6 w-full py-2 rounded-xl text-zinc-400 hover:text-white text-xs font-bold hover:bg-zinc-800 transition-colors"
                                >
                                    Зняти
                                </button>
                            )}
                        </div>
                    </div>

                    {/* RIGHT: Cosmetics grid */}
                    <div className="md:col-span-2 grid grid-cols-1 sm:grid-cols-2 gap-4 content-start">
                        {filteredItems.map(item => (
                            <ItemCard
                                key={item.id}
                                item={item}
                                onCardClick={() => setPreviewItem(prev => prev?.id === item.id ? null : item)}
                            />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};
