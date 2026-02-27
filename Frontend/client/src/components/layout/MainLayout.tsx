// src/components/layout/MainLayout.tsx
import { Outlet } from 'react-router-dom';
import { Header } from './Header';

export const MainLayout = () => {
  return (
    <div className="min-h-screen bg-zinc-950 text-white font-sans selection:bg-purple-500/30">
      {/* Шапка завжди зверху */}
      <Header />
      
      {/* Головний контент сторінки (буде змінюватися) */}
      <main className="max-w-7xl mx-auto p-6 w-full">
        <Outlet />
      </main>
    </div>
  );
};