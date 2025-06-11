-- liquibase formatted sql
-- changeset albina:000_000050 failOnError:true

ALTER TABLE
    danger_source_variants
MODIFY COLUMN
    WEAK_LAYER_CREATION enum(
        'radiation_recrystallization',
        'diurnal_recrystallization',
        'melt_layer_recrystallization',
        'surface_hoar_formation'
    );
