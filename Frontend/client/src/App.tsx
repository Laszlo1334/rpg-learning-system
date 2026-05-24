import { useAuthStore } from '@/store/authStore';
import { authService } from '@/services/authService';

import { useEffect } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

import { Login } from '@/pages/Login';
import { MainLayout } from '@/components/layout/MainLayout';
import { StudentDashboard } from '@/pages/user/StudentDashboard';
import { CoursesPage } from '@/pages/user/CoursesPage';
import { ArenaPage } from '@/pages/user/ArenaPage';
import { FoyerPage } from '@/pages/user/FoyerPage';
import { InventoryPage } from '@/pages/user/InventoryPage';
import { LeaderboardPage } from '@/pages/LeaderboardPage';
import { ShopPage } from '@/pages/ShopPage';
import { ProtectedRoute } from '@/components/ProtectedRoute';

const router = createBrowserRouter([
  {
    path: '/',
    element: <Login />, // Login page renders full-screen, outside the shared header layout
  },
  {
    // All authenticated pages nest under ProtectedRoute, which guards against unauthenticated access
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

  // Verify session on initial page load
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
  if (isLoading) {
    return (
      <div className="min-h-screen bg-zinc-950 flex items-center justify-center text-white">
        <div className="animate-pulse text-xl font-bold">Завантаження світу...</div>
      </div>
    );
  }

  return <RouterProvider router={router} />;
}

export default App;