-- liquibase formatted sql

-- changeset albina:007_000600_drop_region_map_bounds-1
ALTER TABLE regions DROP COLUMN MAP_X_MAX;
-- changeset albina:007_000600_drop_region_map_bounds-2
ALTER TABLE regions DROP COLUMN MAP_X_MIN;
-- changeset albina:007_000600_drop_region_map_bounds-3
ALTER TABLE regions DROP COLUMN MAP_Y_MAX;
-- changeset albina:007_000600_drop_region_map_bounds-4
ALTER TABLE regions DROP COLUMN MAP_Y_MIN;
