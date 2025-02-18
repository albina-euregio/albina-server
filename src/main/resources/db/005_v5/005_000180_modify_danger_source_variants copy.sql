-- liquibase formatted sql
-- changeset albina:000_000050 failOnError:true

ALTER TABLE danger_source_variant_danger_signs CHANGE DANGER_SIGN DANGER_SIGN tinyint(4) DEFAULT NULL;
