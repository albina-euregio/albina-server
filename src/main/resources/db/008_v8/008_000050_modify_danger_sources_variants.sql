-- liquibase formatted sql
-- changeset albina:008_000050 failOnError:true

CREATE TABLE danger_source_variant_aspects_of_existence (
    ASPECT tinyint,
    DANGER_SOURCE_VARIANT_ID varchar(255) not null
) engine=InnoDB;

ALTER TABLE danger_source_variants
    ADD ELEVATION_HIGH_OF_EXISTENCE integer;

ALTER TABLE danger_source_variants
    ADD TREELINE_HIGH_OF_EXISTENCE bit;

ALTER TABLE danger_source_variants
    ADD ELEVATION_LOW_OF_EXISTENCE integer;

ALTER TABLE danger_source_variants
    ADD TREELINE_LOW_OF_EXISTENCE bit;
