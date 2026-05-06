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

    // POST /api/inventory/{id}/equip — slot-exclusive equip with optional targeted replacement
    // replaceId: the inventory entry ID of the item to unequip (used for dual-wield weapon swaps)
    equipItem: async (inventoryId: number, replaceId?: number | null): Promise<void> => {
        const query = replaceId != null ? `?replaceId=${replaceId}` : '';
        await api.post(`/inventory/${inventoryId}/equip${query}`);
    },
};
