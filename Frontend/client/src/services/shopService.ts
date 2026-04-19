// src/services/shopService.ts
import { api } from './api';
import type { Item, InventoryEntry } from '@/types';

export const shopService = {
    // GET /api/items — fetch all available shop items
    getItems: async (): Promise<Item[]> => {
        const response = await api.get<Item[]>('/items');
        return response.data;
    },

    // POST /api/items/{itemId}/buy — purchase an item; returns the new inventory entry
    buyItem: async (itemId: number): Promise<InventoryEntry> => {
        const response = await api.post<InventoryEntry>(`/items/${itemId}/buy`);
        return response.data;
    },
};
