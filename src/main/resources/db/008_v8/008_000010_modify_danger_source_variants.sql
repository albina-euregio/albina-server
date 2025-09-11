-- liquibase formatted sql
-- changeset albina:008_000010 failOnError:true

CREATE TABLE danger_source_variant_weak_layer_grain_shapes (
    WEAK_LAYER_GRAIN_SHAPE varchar(255),
    DANGER_SOURCE_VARIANT_ID varchar(255) not null
) engine=InnoDB;

INSERT INTO danger_source_variant_weak_layer_grain_shapes (WEAK_LAYER_GRAIN_SHAPE, DANGER_SOURCE_VARIANT_ID)
    SELECT WEAK_LAYER_GRAIN_SHAPE, ID
    FROM DANGER_SOURCE_VARIANTS
    WHERE WEAK_LAYER_GRAIN_SHAPE IS NOT NULL;

ALTER TABLE danger_source_variants
    DROP COLUMN WEAK_LAYER_GRAIN_SHAPE;
