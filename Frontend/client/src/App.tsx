// src/App.tsx
import { useEffect } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

// Використовуємо наші нові зручні аліаси @/
import { useAuthStore } from '@/store/authStore';
import { authService } from '@/services/authService';

// Імпортуємо сторінки та Layout
import { Login } from '@/pages/Login';
import { StudentDashboard } from '@/pages/student/StudentDashboard';
import { MainLayout } from '@/components/layout/MainLayout';

// Налаштовуємо маршрути з використанням Layout
const router = createBrowserRouter([
  {
    path: '/',
    element: <Login />, // Сторінка входу залишається на весь екран (без меню)
  },
  {
    // Цей блок відповідає за всі сторінки, де потрібен Header
    path: '/',
    element: <MainLayout />, 
    children: [
      {
        path: 'dashboard',
        element: <StudentDashboard />, // Рендериться всередині <Outlet /> у MainLayout
      },
      // У майбутньому ми просто додаватимемо сюди нові сторінки:
      // { path: 'shop', element: <ShopPage /> },
      // { path: 'leaderboard', element: <LeaderboardPage /> }
    ],
  }
]);

function App() {
  const { isLoading, setUser, setLoading } = useAuthStore();

  // Перевірка авторизації при першому завантаженні сайту
  useEffect(() => {
    const checkAuth = async () => {
      try {
        setLoading(true);
        const userData = await authService.getCurrentUser();
        setUser(userData);
      } catch (error) {
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, [setUser, setLoading]);

  // Показуємо екран завантаження, поки бекенд відповідає
  if (isLoading) {
    return (
      <div className="min-h-screen bg-zinc-950 flex items-center justify-center text-white">
        <div className="animate-pulse text-xl font-bold">Завантаження світу...</div>
      </div>
    );
  }

  // Коли завантаження завершено - віддаємо керування Роутеру
  return <RouterProvider router={router} />;
}

export default App;