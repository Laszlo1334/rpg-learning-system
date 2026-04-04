import { Link, NavLink } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { authService } from '@/services/authService';
import { Coins, Swords, LogOut, Trophy, Store, Gem } from 'lucide-react';

export const Header = () => {
  const { user } = useAuthStore();

  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-2 px-4 py-2 rounded-xl font-bold transition-all ${isActive
      ? 'bg-zinc-800 text-white shadow-sm'
      : 'text-zinc-400 hover:text-white hover:bg-zinc-800/50'
    }`;

  return (
    <header className="sticky top-0 z-50 w-full bg-zinc-950 border-b border-zinc-800 px-6 py-3">
      <div className="max-w-7xl mx-auto flex items-center justify-between">

        {/* 1. Логотип */}
        <Link to="/dashboard" className="flex items-center gap-2 text-2xl font-black text-white">
          <span className="text-purple-500">Edu</span>RPG
        </Link>

        {/* 2. Навігація по сторінках */}
        <nav className="hidden md:flex items-center gap-2">
          <NavLink to="/courses" className={navLinkClass}>
            <Swords size={20} />
            <span>Квести</span>
          </NavLink>
          <NavLink to="/shop" className={navLinkClass}>
            <Store size={20} />
            <span>Магазин</span>
          </NavLink>
          <NavLink to="/leaderboard" className={navLinkClass}>
            <Trophy size={20} />
            <span>Рейтинг</span>
          </NavLink>
        </nav>

        {/* 3. Статистика гравця та кнопка виходу */}
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-3 bg-zinc-900 border border-zinc-800 px-4 py-1.5 rounded-full shadow-inner">
            <div className="flex items-center gap-1.5 text-yellow-400 font-bold" title="Монети">
              <Coins size={18} />
              <span>{user?.gold || 0}</span>
            </div>
            <div className="w-px h-4 bg-zinc-700"></div>
            <div className="flex items-center gap-1.5 text-purple-400 font-bold" title="Кристали">
              <Gem size={18} />
              <span className="mt-0.5">{user?.crystals || 0}</span>
            </div>
          </div>

          <button
            onClick={authService.logout}
            className="p-2 text-zinc-400 hover:text-red-400 hover:bg-red-400/10 rounded-xl transition-colors"
            title="Вийти з акаунта"
          >
            <LogOut size={20} />
          </button>
        </div>

      </div>
    </header>
  );
};