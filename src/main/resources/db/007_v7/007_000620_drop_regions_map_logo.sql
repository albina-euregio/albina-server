-- liquibase formatted sql

-- changeset albina:007_000620_drop_regions_map_logo-1
ALTER TABLE regions DROP COLUMN MAP_LOGO_COLOR_PATH;
-- changeset albina:007_000620_drop_regions_map_logo-2
ALTER TABLE regions DROP COLUMN MAP_LOGO_BW_PATH;
