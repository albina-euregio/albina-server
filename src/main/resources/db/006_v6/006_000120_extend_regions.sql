-- liquibase formatted sql
-- changeset albina:006_000120 failOnError:true

ALTER TABLE regions ADD ENABLE_DANGER_SOURCES bit DEFAULT 0 NULL;
UPDATE regions SET ENABLE_DANGER_SOURCES=1 WHERE ID='AT-07';
