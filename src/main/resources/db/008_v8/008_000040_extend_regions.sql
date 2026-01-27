-- liquibase formatted sql
-- changeset albina:006_000090 failOnError:true

ALTER TABLE regions ADD ENABLE_LINEA_EXPORT bit DEFAULT 0 NULL;
UPDATE regions SET ENABLE_LINEA_EXPORT=1 WHERE ID='AT-07';