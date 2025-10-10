-- liquibase formatted sql

-- changeset albina:007_000610_drop_regions_map_center-1
ALTER TABLE regions DROP COLUMN MAP_CENTER_LAT;
-- changeset albina:007_000610_drop_regions_map_center-2
ALTER TABLE regions DROP COLUMN MAP_CENTER_LNG;
