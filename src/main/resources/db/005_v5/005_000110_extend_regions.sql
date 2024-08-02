-- liquibase formatted sql
-- changeset albina:000_000050 failOnError:true

ALTER TABLE regions ADD ENABLE_STRESS_LEVEL bit DEFAULT 0 NULL;
UPDATE regions SET ENABLE_STRESS_LEVEL=1 WHERE ID='AT-07';
