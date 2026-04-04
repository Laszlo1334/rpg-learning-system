import { Flame } from 'lucide-react';

// Визначаємо, які дані має отримати віджет ззовні
interface CampfireWidgetProps {
    level: number;
}

export const CampfireWidget = ({ level }: CampfireWidgetProps) => {
    // Словник налаштувань для кожного рівня багаття
    const campfireConfigs = {
        1: {
            color: 'text-zinc-500',
            glow: 'shadow-none',
            title: 'Тліюче вугілля',
            desc: 'Рівень 1: Заходь щодня, щоб розпалити полум\'я (+2% до монет).'
        },
        2: {
            color: 'text-yellow-500',
            glow: 'shadow-[0_0_15px_rgba(234,179,8,0.15)]',
            title: 'Іскра',
            desc: 'Рівень 2: +5% до всіх знайдених монет.'
        },
        3: {
            color: 'text-orange-500',
            glow: 'shadow-[0_0_20px_rgba(249,115,22,0.2)]',
            title: 'Вогнище',
            desc: 'Рівень 3: +10% до монет та досвіду.'
        },
        4: {
            color: 'text-red-500',
            glow: 'shadow-[0_0_25px_rgba(239,68,68,0.3)]',
            title: 'Палаюче багаття',
            desc: 'Рівень 4: +20% до нагород та шанс на скриньку.'
        },
        5: {
            color: 'text-purple-500',
            glow: 'shadow-[0_0_30px_rgba(168,85,247,0.4)]',
            title: 'Магічне полум\'я',
            desc: 'Рівень 5: +30% до всіх нагород. Максимальний бонус!'
        },
    };

    // Захист від помилок: гарантуємо, що рівень завжди в межах 1-5
    const safeLevel = Math.max(1, Math.min(5, level)) as 1 | 2 | 3 | 4 | 5;
    const config = campfireConfigs[safeLevel];

    return (
        <div className={`h-full bg-zinc-900 border border-zinc-800 rounded-2xl p-6 flex flex-col items-center justify-center min-h-[160px] text-center transition-all duration-500 hover:border-zinc-700 ${config.glow}`}>
            {/* Анімація animate-pulse створює ефект "дихання" вогню */}
            <Flame size={48} className={`mb-3 ${config.color} animate-pulse`} />
            <h3 className="font-bold text-white mb-2">{config.title}</h3>
            <p className="text-xs text-zinc-400 leading-relaxed">{config.desc}</p>
        </div>
    );
};