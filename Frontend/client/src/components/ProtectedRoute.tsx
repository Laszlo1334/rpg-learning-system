// src/components/ProtectedRoute.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

export const ProtectedRoute = () => {
  const { user, isLoading } = useAuthStore();

  // Поки йде перевірка авторизації — нічого не рендеримо
  if (isLoading) {
    return (
      <div className="min-h-screen bg-zinc-950 flex items-center justify-center text-white">
        <div className="animate-pulse text-xl font-bold">Завантаження світу...</div>
      </div>
    );
  }

  // Якщо завантаження завершено і юзера немає — редіректимо на login
  if (!user) {
    return <Navigate to="/" replace />;
  }

  // Якщо юзер авторизований — рендеримо дочірні маршрути
  return <Outlet />;
};
