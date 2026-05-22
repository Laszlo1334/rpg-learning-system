import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { authService } from '@/services/authService';
import { Coins, Swords, LogOut, Trophy, Store, Gem, Tent } from 'lucide-react';

export const Header = () => {
  const { user, setUser } = useAuthStore();
  const navigate = useNavigate();
  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);

  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-2 px-4 py-2 rounded-xl font-bold transition-all ${isActive
      ? 'bg-zinc-800 text-white shadow-sm'
      : 'text-zinc-400 hover:text-white hover:bg-zinc-800/50'
    }`;

  const handleLogoutConfirm = async () => {
    try {
      await authService.logout();
    } catch {
    } finally {
      setUser(null);
      setIsLogoutModalOpen(false);
      navigate('/');
    }
  };

  return (
    <>
      <header className="sticky top-0 z-50 w-full bg-zinc-950 border-b border-zinc-800 px-6 py-3">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-3 md:gap-0">

        <Link to="/dashboard" className="flex items-center gap-2 text-2xl font-black text-white shrink-0">
            <span className="text-purple-500">Edu</span>RPG
          </Link>

          <nav className="flex items-center gap-2 overflow-x-auto whitespace-nowrap scrollbar-hide max-w-full pb-1 md:pb-0">
            <NavLink to="/dashboard" className={navLinkClass}>
              <Tent size={20} />
              <span>Табір</span>
            </NavLink>
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

          <div className="flex items-center gap-4 shrink-0">
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
              onClick={() => setIsLogoutModalOpen(true)}
              className="p-2 text-zinc-400 hover:text-red-400 hover:bg-red-400/10 rounded-xl transition-colors"
              title="Вийти з акаунта"
            >
              <LogOut size={20} />
            </button>
          </div>

        </div>
      </header>

      {isLogoutModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm">
          <div className="bg-zinc-900 border border-zinc-800 rounded-2xl p-6 max-w-sm w-full mx-4 shadow-2xl">
            <div className="flex flex-col items-center text-center gap-4">
              <div className="w-14 h-14 bg-red-500/10 rounded-full flex items-center justify-center">
                <LogOut size={28} className="text-red-400" />
              </div>
              <h2 className="text-xl font-black text-white">Покинути Табір?</h2>
              <p className="text-zinc-400 text-sm">Ви впевнені, що хочете покинути Табір?</p>
            </div>
            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setIsLogoutModalOpen(false)}
                className="flex-1 py-3 rounded-xl font-bold bg-zinc-800 text-zinc-200 hover:bg-zinc-700 transition-colors"
              >
                Залишитись
              </button>
              <button
                onClick={handleLogoutConfirm}
                className="flex-1 py-3 rounded-xl font-bold bg-red-600 text-white hover:bg-red-500 transition-colors"
              >
                Вийти
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};