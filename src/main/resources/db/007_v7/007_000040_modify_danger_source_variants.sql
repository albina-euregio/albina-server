-- liquibase formatted sql
-- changeset albina:007_000040 failOnError:true

ALTER TABLE
    danger_source_variants
MODIFY COLUMN
    WEAK_LAYER_CREATION enum(
        'radiation_recrystallization',
        'diurnal_recrystallization',
        'melt_layer_recrystallization',
        'surface_hoar_formation'
    );
