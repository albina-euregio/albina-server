-- liquibase formatted sql
-- changeset albina:000_000050 failOnError:true

ALTER TABLE danger_source_variants ADD SNOW_HEIGHT_AVERAGE integer DEFAULT 0;
