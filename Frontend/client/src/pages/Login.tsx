// src/pages/Login.tsx
import { authService } from '../services/authService';
import { useAuthStore } from '../store/authStore';
import { Navigate } from 'react-router-dom';

export const Login = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  // Якщо юзер вже залогінений, але випадково зайшов на сторінку логіну - кидаємо його на дашборд
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div className="min-h-screen bg-zinc-950 flex flex-col items-center justify-center text-white gap-6">
      <h1 className="text-4xl font-bold">EduRPG System</h1>
      <p className="text-zinc-400">Навчайся граючи. Виконуй квести. Здобувай славу.</p>
      
      <button 
        onClick={authService.loginWithGoogle}
        className="bg-white text-black px-6 py-3 rounded-xl font-bold hover:bg-zinc-200 transition-colors"
      >
        Увійти через Google
      </button>
    </div>
  );
};