-- Remove duplicate items keeping only the oldest entry (lowest id) per name
DELETE FROM items
WHERE id IN (
    SELECT id FROM (
        SELECT id, ROW_NUMBER() OVER(PARTITION BY name ORDER BY id) AS row_num
        FROM items
    ) t WHERE t.row_num > 1
);
