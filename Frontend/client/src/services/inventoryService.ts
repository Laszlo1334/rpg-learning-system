// src/services/inventoryService.ts
import { api } from './api';
import type { InventoryEntry } from '@/types';

export const inventoryService = {
    // GET /api/inventory — fetch current player's bag
    getInventory: async (): Promise<InventoryEntry[]> => {
        const response = await api.get<InventoryEntry[]>('/inventory');
        return response.data;
    },

    // POST /api/inventory/{id}/use — consume one unit of a consumable, activates its effect
    useItem: async (inventoryId: number): Promise<void> => {
        await api.post(`/inventory/${inventoryId}/use`);
    },

    // POST /api/inventory/{id}/equip — toggle equipped state for a cosmetic item
    toggleEquip: async (inventoryId: number): Promise<void> => {
        await api.post(`/inventory/${inventoryId}/equip`);
    },
};
