import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { inventoryService } from '@/services/inventoryService';
import type { InventoryEntry } from '@/types';
import { ArrowLeft, Shirt, Crown, Image as ImageIcon } from 'lucide-react';

export const InventoryPage = () => {
    const navigate = useNavigate();
    const { refreshUser } = useAuthStore();
    const [inventory, setInventory] = useState<InventoryEntry[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    const fetchInventory = async () => {
        try {
            const data = await inventoryService.getInventory();
            setInventory(data.filter((entry: InventoryEntry) => entry.item.category === 'COSMETIC'));
        } catch (error) {
            console.error('Помилка завантаження інвентарю', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchInventory();
    }, []);

    const handleToggleEquip = async (id: number) => {
        try {
            await inventoryService.toggleEquip(id);
            await Promise.all([fetchInventory(), refreshUser()]);
        } catch (error) {
            console.error('Не вдалося екіпірувати предмет', error);
        }
    };

    if (isLoading) {
        return (
            <div className="min-h-screen bg-zinc-950 flex items-center justify-center text-zinc-500 font-bold">
                Відкриваємо скрині...
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-zinc-950 text-white p-6">
            <div className="max-w-6xl mx-auto">

                <header className="flex items-center justify-between mb-8 pb-4 border-b border-zinc-800">
                    <div className="flex items-center gap-4">
                        <button
                            onClick={() => navigate('/dashboard')}
                            className="p-2 bg-zinc-900 border border-zinc-700 rounded-xl hover:bg-zinc-800 transition-colors"
                        >
                            <ArrowLeft size={24} />
                        </button>
                        <h1 className="text-3xl font-black text-purple-400 flex items-center gap-3">
                            <Shirt /> Гардеробна
                        </h1>
                    </div>
                </header>

                {inventory.length === 0 ? (
                    <div className="text-center py-20 bg-zinc-900/50 rounded-3xl border border-zinc-800">
                        <Crown size={48} className="mx-auto text-zinc-600 mb-4" />
                        <p className="text-xl text-zinc-400 font-bold mb-2">Ваш гардероб порожній</p>
                        <p className="text-zinc-500">
                            Завітайте до Крамниці Гільдії, щоб придбати нове спорядження.
                        </p>
                        <button
                            onClick={() => navigate('/shop')}
                            className="mt-6 px-6 py-3 bg-purple-600 hover:bg-purple-500 rounded-xl font-bold transition-all shadow-[0_0_15px_rgba(168,85,247,0.4)]"
                        >
                            Перейти до Крамниці
                        </button>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {inventory.map((entry) => (
                            <div
                                key={entry.id}
                                className={`p-6 rounded-2xl border transition-all duration-300 relative overflow-hidden ${
                                    entry.isEquipped
                                        ? 'bg-purple-900/20 border-purple-500 shadow-[0_0_20px_rgba(168,85,247,0.15)]'
                                        : 'bg-zinc-900 border-zinc-800 hover:border-zinc-600'
                                }`}
                            >
                                {entry.isEquipped && (
                                    <div className="absolute top-0 right-0 bg-purple-500 text-white text-xs font-black px-4 py-1 rounded-bl-xl uppercase tracking-widest z-10">
                                        Надіто
                                    </div>
                                )}

                                <div className="w-full h-40 bg-zinc-950 rounded-xl mb-4 flex items-center justify-center border border-zinc-800">
                                    {entry.item.assetUrl ? (
                                        <img
                                            src={entry.item.assetUrl}
                                            alt={entry.item.name}
                                            className="max-h-full object-contain p-4"
                                        />
                                    ) : (
                                        <ImageIcon size={40} className="text-zinc-700" />
                                    )}
                                </div>

                                <h3 className="text-xl font-bold mb-1">{entry.item.name}</h3>
                                <p className="text-zinc-400 text-sm mb-6 h-10">{entry.item.description}</p>

                                <button
                                    onClick={() => handleToggleEquip(entry.id)}
                                    className={`w-full py-3 rounded-xl font-bold transition-all ${
                                        entry.isEquipped
                                            ? 'bg-zinc-800 text-zinc-300 hover:bg-zinc-700'
                                            : 'bg-white text-black hover:bg-zinc-200'
                                    }`}
                                >
                                    {entry.isEquipped ? 'Зняти' : 'Екіпірувати'}
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};
