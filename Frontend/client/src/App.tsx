// src/App.tsx
import { useEffect } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

// Використовуємо наші нові зручні аліаси @/
import { useAuthStore } from '@/store/authStore';
import { authService } from '@/services/authService';

// Імпортуємо сторінки та Layout
import { Login } from '@/pages/Login';
import { MainLayout } from '@/components/layout/MainLayout';
import { StudentDashboard } from '@/pages/student/StudentDashboard';
import { CoursesPage } from '@/pages/student/CoursesPage';
import { ArenaPage } from '@/pages/student/ArenaPage';
import { FoyerPage } from '@/pages/student/FoyerPage';
import { InventoryPage } from '@/pages/student/InventoryPage';
import { LeaderboardPage } from '@/pages/LeaderboardPage';
import { ShopPage } from '@/pages/ShopPage';
import { ProtectedRoute } from '@/components/ProtectedRoute';

// Налаштовуємо маршрути з використанням Layout
const router = createBrowserRouter([
  {
    path: '/',
    element: <Login />, // Сторінка входу залишається на весь екран (без меню)
  },
  {
    // Цей блок відповідає за всі сторінки, де потрібен Header / Sidebar
    // ProtectedRoute перевіряє авторизацію перед рендером будь-якого дочірнього маршруту
    path: '/',
    element: <ProtectedRoute />,
    children: [
      {
        path: '/',
        element: <MainLayout />,
        children: [
          {
            path: 'dashboard',
            element: <StudentDashboard />,
          },
          {
            path: 'courses',
            element: <CoursesPage />,
          },
          {
            path: 'courses/:courseId/foyer',
            element: <FoyerPage />,
          },
          {
            path: 'arena/:id',
            element: <ArenaPage />,
          },
          {
            path: 'leaderboard',
            element: <LeaderboardPage />,
          },
          {
            path: 'shop',
            element: <ShopPage />,
          },
          {
            path: 'inventory',
            element: <InventoryPage />,
          }
        ],
      }
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