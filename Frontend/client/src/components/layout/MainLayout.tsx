
import { Outlet, useLocation } from 'react-router-dom';
import { Header } from './Header';

export const MainLayout = () => {
  const location = useLocation();
  const isMapPage = location.pathname.match(/^\/courses\/\d+$/) || location.pathname.endsWith('/foyer');

  return (
    <div className="min-h-screen bg-[#FBF7F0] dark:bg-zinc-950 text-[#4A3B2F] dark:text-white font-sans selection:bg-purple-500/30 flex flex-col">
      <Header />
      
      {isMapPage ? (
        <main className="flex-1 w-full relative h-[calc(100vh-73px)]">
          <Outlet />
        </main>
      ) : (
        <main className="max-w-7xl mx-auto p-6 w-full">
          <Outlet />
        </main>
      )}
    </div>
  );
};