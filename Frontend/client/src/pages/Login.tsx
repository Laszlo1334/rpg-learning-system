import { authService } from '../services/authService';
import { useAuthStore } from '../store/authStore';
import { Navigate } from 'react-router-dom';

export const Login = () => {
  const user = useAuthStore((state) => state.user);

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div className="min-h-screen bg-[#FBF7F0] dark:bg-zinc-950 flex flex-col items-center justify-center text-[#4A3B2F] dark:text-white gap-6">
      <h1 className="text-4xl font-bold">EduRPG System</h1>
      <p className="text-[#8C7A65] dark:text-zinc-400">Навчайся граючи. Виконуй квести. Здобувай славу.</p>

      <button
        onClick={authService.loginWithGoogle}
        className="bg-[#F6F1E6] dark:bg-white text-[#4A3B2F] dark:text-black border border-[#D6CAB4] dark:border-transparent px-6 py-3 rounded-xl font-bold hover:bg-[#EDE6D6] dark:hover:bg-zinc-200 transition-colors shadow-sm"
      >
        Увійти через Google
      </button>
    </div>
  );
};