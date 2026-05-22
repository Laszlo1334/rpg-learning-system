
import { Outlet } from 'react-router-dom';
import { Header } from './Header';

export const MainLayout = () => {
  return (
    <div className="min-h-screen bg-zinc-950 text-white font-sans selection:bg-purple-500/30">
      <Header />
      
      <main className="max-w-7xl mx-auto p-6 w-full">
        <Outlet />
      </main>
    </div>
  );
};