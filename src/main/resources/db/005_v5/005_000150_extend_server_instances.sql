-- liquibase formatted sql
-- changeset albina:000_000050 failOnError:true

ALTER TABLE server_instances ADD DANGER_LEVEL_ELEVATION_DEPENDENCY bit DEFAULT 1 NULL;
