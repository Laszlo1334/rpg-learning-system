export type Role = 'STUDENT' | 'TEACHER' | 'ADMIN';
export type QuestionType = 'TEST' | 'TEXT';
export type CurrencyType = 'GOLD' | 'CRYSTAL';
export type ItemCategory = 'COSMETIC' | 'CONSUMABLE';
export type ItemEffect = 'XP_BOOST' | 'GOLD_BOOST' | 'ENERGY_REFILL' | 'SHIELD' | 'NONE';
export type ItemSlot = 'HEAD' | 'BODY' | 'HANDS' | 'LEGS' | 'WEAPON' | 'BACKGROUND' | 'FRAME' | 'NONE';
export type SubmissionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface User {
  id: number;
  username: string;
  email: string;
  password?: string;
  role: Role;
  avatarUrl: string | null;

  level: number;
  currentXp: number;
  gold: number;
  crystals: number;

  campfireLevel: number;
  lastLoginDate: string | null;

  energy: number;
  lastTaskCompletionDate: string | null;

  isPublicProfile: boolean;

  xpBuffEndsAt: string | null;
  goldBuffEndsAt: string | null;
  energyStasisEndsAt: string | null;
  hasActiveShield: boolean;

  lifetimeGold: number;
  lifetimeCrystals: number;
  totalTasksCompleted: number;
  totalFailures: number;
}

export interface UserStatsDto {
  lifetimeGold: number;
  lifetimeCrystals: number;
  totalTasksCompleted: number;
  totalFailures: number;
}

export interface CourseProgressDto {
  id: number;
  title: string;
  description: string;
  totalTasks: number;
  completedTasks: number;
  progressPercentage: number;
}

export interface QuestionDto {
  id: number;
  questionText: string;
  type: QuestionType;
  options: string[];
}

export interface TaskDto {
  id: number;
  title: string;
  theoryContent: string;
  branchName: string;
  orderIndex: number;
  isTheoryHidden: boolean;
  rewardXp: number;
  rewardGold: number;
  isCompleted: boolean;
  isLocked: boolean;
  prerequisiteTaskIds: number[];
  questions: QuestionDto[];
  dynamicQuestionCount?: number;
  type?: 'REGULAR' | 'BOSS' | 'MEMORY';
  bossMetadata?: {
    bossName: string;
    bossAvatar: string;
    timeLimitSeconds?: number;
  };
}

export interface AnswerRequest {
  questionId: number;
  userAnswer: string;
}

export interface AnswerResponse {
  isCorrect: boolean;
  explanation: string | null;
  crystalsAwarded: number | null;
}

export interface RunCompletionRequest {
  taskId: number;
  isVictory: boolean;
  failedQuestionIds: number[];
}

export interface Item {
  id: number;
  name: string;
  description: string;
  price: number;
  currencyType: CurrencyType;
  category: ItemCategory;
  effect: ItemEffect;
  slot: ItemSlot; // Slot this item occupies
  assetUrl: string | null;
}

export interface InventoryEntry {
  id: number;
  item: Item;
  isEquipped: boolean;
  quantity: number;
  purchasedAt: string;
}

export interface LeaderboardDto {
  id: number;
  username: string;
  level: number;
  xp: number;
  avatarUrl: string | null;
}

export interface CourseLeaderboardDto {
  userId: number;
  username: string;
  courseXp: number;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: Role;
}

export interface CourseDto {
  id: number;
  title: string;
  description: string;
  totalTasks: number;
  completedTasks: number;
  status: 'new' | 'in_progress' | 'completed' | 'locked';
  rewardIcon?: string; // Наприклад, 'sword', 'shield', 'scroll'
}

