-- liquibase formatted sql

-- changeset christina:1749648735185-3
ALTER TABLE danger_source_variants MODIFY DANGER_SOURCE_ID VARCHAR(191);

-- changeset christina:1749648735185-4
ALTER TABLE danger_source_variant_aspects MODIFY DANGER_SOURCE_VARIANT_ID VARCHAR(191);

-- changeset christina:1749648735185-5
ALTER TABLE danger_source_variant_danger_signs MODIFY DANGER_SOURCE_VARIANT_ID VARCHAR(191);

-- changeset christina:1749648735185-6
ALTER TABLE danger_source_variant_regions MODIFY DANGER_SOURCE_VARIANT_ID VARCHAR(191);

-- changeset christina:1749648735185-7
ALTER TABLE danger_source_variant_terrain_types MODIFY DANGER_SOURCE_VARIANT_ID VARCHAR(191);

-- changeset christina:1749648735185-9
ALTER TABLE danger_source_variants MODIFY ID VARCHAR(191);

-- changeset christina:1749648735185-10
ALTER TABLE danger_sources MODIFY ID VARCHAR(191);
