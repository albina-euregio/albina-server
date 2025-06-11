-- liquibase formatted sql
-- changeset albina:000_000050 failOnError:true

ALTER TABLE danger_source_variants
    CHANGE COLUMN WEAK_LAYER_CRUST_ABOVE WEAK_LAYER_CRUST_ABOVE_OLD BIT(1);

ALTER TABLE danger_source_variants
    ADD COLUMN WEAK_LAYER_CRUST_ABOVE ENUM ('no', 'partly', 'yes');

UPDATE danger_source_variants
    SET WEAK_LAYER_CRUST_ABOVE = IF(WEAK_LAYER_CRUST_ABOVE_OLD, 'yes', 'no');

ALTER TABLE danger_source_variants
    DROP COLUMN WEAK_LAYER_CRUST_ABOVE_OLD;


ALTER TABLE danger_source_variants
    CHANGE COLUMN WEAK_LAYER_CRUST_BELOW WEAK_LAYER_CRUST_BELOW_OLD BIT(1);

ALTER TABLE danger_source_variants
    ADD COLUMN WEAK_LAYER_CRUST_BELOW ENUM ('no', 'partly', 'yes');

UPDATE danger_source_variants
    SET WEAK_LAYER_CRUST_BELOW = IF(WEAK_LAYER_CRUST_BELOW_OLD, 'yes', 'no');

ALTER TABLE danger_source_variants
    DROP COLUMN WEAK_LAYER_CRUST_BELOW_OLD;
