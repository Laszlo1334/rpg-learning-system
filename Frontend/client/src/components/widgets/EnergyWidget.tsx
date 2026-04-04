import { Zap } from 'lucide-react';

interface EnergyWidgetProps {
    energy: number;
}

export const EnergyWidget = ({ energy }: EnergyWidgetProps) => {
    // Запобіжник: гарантуємо, що значення завжди між 0 та 100
    const safeEnergy = Math.max(0, Math.min(100, energy));
    // Перевіряємо, чи є бонус (енергія більше нуля)
    const hasBonus = safeEnergy > 0;

    return (
        <div className="h-full bg-zinc-900 border border-zinc-800 rounded-2xl p-6 flex flex-col items-center justify-center min-h-[160px] text-center transition-all duration-300 hover:border-zinc-700">
            {/* Іконка блискавки: світиться жовтим, якщо є енергія */}
            <Zap
                size={36}
                className={`mb-3 transition-colors ${hasBonus ? 'text-yellow-400 drop-shadow-[0_0_10px_rgba(250,204,21,0.5)]' : 'text-zinc-600'}`}
            />

            <h3 className="font-bold text-white mb-3">Енергія Відпочинку</h3>

            {/* Лінійна шкала */}
            <div className="w-full bg-zinc-950 rounded-full h-3 mb-2 border border-zinc-800 relative overflow-hidden">
                <div
                    className={`h-full rounded-full transition-all duration-700 ${hasBonus ? 'bg-yellow-400 shadow-[0_0_10px_rgba(250,204,21,0.8)]' : 'bg-zinc-700'}`}
                    style={{ width: `${safeEnergy}%` }}
                ></div>
            </div>

            {/* Інформація про множник */}
            <div className="flex justify-between w-full text-xs font-bold mb-2">
                <span className="text-zinc-400">{safeEnergy} / 100</span>
                <span className={hasBonus ? 'text-yellow-400' : 'text-zinc-500'}>
                    {hasBonus ? 'Бонус: x1.5' : 'Бонус: x1.0'}
                </span>
            </div>

            <p className="text-[10px] text-zinc-500 leading-tight">
                Відновлюється автоматично під час відпочинку.
            </p>
        </div>
    );
};