import { api } from './api';
import type { Item, InventoryEntry } from '@/types';

export const shopService = {
    getItems: async (): Promise<Item[]> => {
        const response = await api.get<Item[]>('/items');
        return response.data;
    },

    buyItem: async (itemId: number): Promise<InventoryEntry> => {
        const response = await api.post<InventoryEntry>(`/items/${itemId}/buy`);
        return response.data;
    },
};
