import { api } from './api';
import type { User, UserStatsDto } from '../types';

export const authService = {
  // Get current authenticated user profile
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<User>('/users/me');
    return response.data;
  },

  // Get detailed lifetime stats for Chronicle modal
  getUserStats: async (): Promise<UserStatsDto> => {
    const response = await api.get<UserStatsDto>('/users/me/stats');
    return response.data;
  },

  // Burn the active Rune of Protection shield (called after it absorbs a hit)
  consumeShield: async (): Promise<void> => {
    await api.post('/users/me/consume-shield');
  },

  loginWithGoogle: () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  },

  logout: async () => {
    await api.post('/logout');
  }
};