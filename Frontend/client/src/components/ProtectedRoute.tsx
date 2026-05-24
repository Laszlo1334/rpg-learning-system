import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

export const ProtectedRoute = () => {
  const { user, isLoading } = useAuthStore();

  // Suppress rendering while auth state is being resolved
  if (isLoading) {
    return (
      <div className="min-h-screen bg-[#FBF7F0] dark:bg-zinc-950 flex items-center justify-center text-[#4A3B2F] dark:text-white">
        <div className="animate-pulse text-xl font-bold">Завантаження світу...</div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};
