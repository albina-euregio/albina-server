-- liquibase formatted sql
-- changeset albina:006_000090 failOnError:true

ALTER TABLE regions ADD ENABLE_WEATHERBOX bit DEFAULT 0 NULL;
UPDATE regions SET ENABLE_WEATHERBOX=1 WHERE ID='AT-07';
