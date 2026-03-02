-- liquibase formatted sql
-- changeset albina:008_000640 failOnError:true

ALTER TABLE danger_source_variants
    ADD COLUMN WEAK_LAYER_GRAIN_SHAPES set ('PP', 'MM', 'DF', 'RG', 'FC', 'DH', 'SH', 'MF', 'IF', 'PPco', 'PPnd', 'PPpl', 'PPsd', 'PPir', 'PPgp', 'PPhl', 'PPip', 'PPrm', 'MMrp', 'MMci', 'DFdc', 'DFbk', 'RGsr', 'RGlr', 'RGwp', 'RGxf', 'FCso', 'FCsf', 'FCxr', 'DHcp', 'DHpr', 'DHch', 'DHla', 'DHxr', 'SHsu', 'SHcv', 'SHxr', 'MFcl', 'MFpc', 'MFsl', 'MFcr', 'IFil', 'IFic', 'IFbi', 'IFrc', 'IFsc');

UPDATE danger_source_variants
SET WEAK_LAYER_GRAIN_SHAPES =
        (select GROUP_CONCAT(WEAK_LAYER_GRAIN_SHAPE)
         from danger_source_variant_weak_layer_grain_shapes
         where danger_source_variants.ID =
               danger_source_variant_weak_layer_grain_shapes.DANGER_SOURCE_VARIANT_ID);

DROP TABLE danger_source_variant_weak_layer_grain_shapes;
