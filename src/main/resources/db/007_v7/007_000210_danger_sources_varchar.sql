-- liquibase formatted sql

-- changeset albina:007_000210-1
ALTER TABLE danger_source_variants MODIFY DANGER_SOURCE_ID VARCHAR(191);

-- changeset albina:007_000210-2
ALTER TABLE danger_source_variant_aspects MODIFY DANGER_SOURCE_VARIANT_ID VARCHAR(191);

-- changeset albina:007_000210-3
ALTER TABLE danger_source_variant_danger_signs MODIFY DANGER_SOURCE_VARIANT_ID VARCHAR(191);

-- changeset albina:007_000210-4
ALTER TABLE danger_source_variant_regions MODIFY DANGER_SOURCE_VARIANT_ID VARCHAR(191);

-- changeset albina:007_000210-5
ALTER TABLE danger_source_variant_terrain_types MODIFY DANGER_SOURCE_VARIANT_ID VARCHAR(191);

-- changeset albina:007_000210-6
ALTER TABLE danger_source_variants MODIFY ID VARCHAR(191);

-- changeset albina:007_000210-7
ALTER TABLE danger_sources MODIFY ID VARCHAR(191);
