import { create } from 'zustand';
import { api } from '../services/api';
import type { User } from '../types';

interface AuthState {
  user: User | null;
  isLoading: boolean;

  setUser: (user: User | null) => void;
  setLoading: (status: boolean) => void;

  // Fetches fresh user data from the backend and updates the store
  refreshUser: () => Promise<void>;

  // Optimistic currency deductions for the shop flow
  spendGold: (amount: number) => boolean;
  spendCrystals: (amount: number) => boolean;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  isLoading: true,

  setUser: (user) => set({ user }),
  setLoading: (status) => set({ isLoading: status }),

  refreshUser: async () => {
    try {
      const response = await api.get<User>('/users/me');
      set({ user: response.data });
    } catch {
      // Silently swallow: stale data is preferable to crashing the UI
    }
  },

  spendGold: (amount) => {
    const currentUser = get().user;
    if (!currentUser) return false;
    if (currentUser.gold < amount) return false;

    set({ user: { ...currentUser, gold: currentUser.gold - amount } });
    return true;
  },

  spendCrystals: (amount) => {
    const currentUser = get().user;
    if (!currentUser) return false;
    if (currentUser.crystals < amount) return false;

    set({ user: { ...currentUser, crystals: currentUser.crystals - amount } });
    return true;
  },
}));