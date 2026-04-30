-- liquibase formatted sql
-- changeset albina:008_001000 failOnError:true

ALTER TABLE regions ADD ENABLE_ICON bit DEFAULT 0 NULL;
