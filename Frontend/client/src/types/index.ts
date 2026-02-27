// src/types/index.ts

export type Role = 'STUDENT' | 'TEACHER' | 'ADMIN';
export type VerificationType = 'AUTO' | 'MANUAL';

// 1. Опис Користувача (GET /api/users/me)
export interface User {
  id: number;
  username: string;
  email: string;
  role: Role;
  level: number;
  xp: number;
  coins: number;
  avatarUrl: string | null;
  createdAt: string; 
  // Примітка: пароль ми ігноруємо на фронтенді заради безпеки
}

// 2. Опис Курсу (бо він приходить всередині Task)
export interface Course {
  id: number;
  title: string;
  description: string;
  accessCode: string;
  createdAt: string;
}

// 3. Опис Квесту (GET /api/tasks)
export interface Task {
  id: number;
  title: string;
  description: string;
  course?: Course; // Зробили опціональним на випадок, якщо квест без курсу
  rewardXp: number;
  rewardCoins: number;
  verificationType: VerificationType;
  correctAnswer?: string; 
}

// 4. Опис гравця в Лідерборді (GET /api/users/leaderboard)
export interface LeaderboardEntry {
  id: number;
  username: string;
  level: number;
  xp: number;
  avatarUrl: string | null;
}