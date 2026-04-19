import { create } from 'zustand';
import { api } from '../services/api';
import type { User } from '../types';

// Описуємо, що саме буде зберігатися в нашому "сховищі"
interface AuthState {
  user: User | null;         // Дані поточного гравця (якщо null - гравець не авторизований)
  isLoading: boolean;        // Статус завантаження даних

  // Дії (Actions) для зміни стану
  setUser: (user: User | null) => void;
  setLoading: (status: boolean) => void;

  // ✅ Оновлення профілю після завершення забігу (GET /users/me)
  refreshUser: () => Promise<void>;

  // Спеціальні дії для Магазину
  spendGold: (amount: number) => boolean;
  spendCrystals: (amount: number) => boolean;
}

// Створюємо саме сховище
export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  isLoading: true,

  setUser: (user) => set({ user }),
  setLoading: (status) => set({ isLoading: status }),

  // Робить свіжий запит на бекенд і оновлює дані гравця в сторі
  refreshUser: async () => {
    try {
      const response = await api.get<User>('/users/me');
      set({ user: response.data });
    } catch {
      // Тихо ігноруємо: якщо запит не вдався, старі дані залишаються
    }
  },

  // Логіка витрачання золота
  spendGold: (amount) => {
    const currentUser = get().user;
    if (!currentUser) return false; // Якщо гравця немає, скасовуємо
    if (currentUser.gold < amount) return false; // Якщо не вистачає грошей, скасовуємо

    // Оновлюємо стан: віднімаємо золото
    set({
      user: {
        ...currentUser,
        gold: currentUser.gold - amount,
      },
    });
    return true; // Покупка успішна
  },

  // Логіка витрачання кристалів
  spendCrystals: (amount) => {
    const currentUser = get().user;
    if (!currentUser) return false;
    if (currentUser.crystals < amount) return false;

    set({
      user: {
        ...currentUser,
        crystals: currentUser.crystals - amount,
      },
    });
    return true;
  },
}));