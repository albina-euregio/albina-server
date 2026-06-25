-- liquibase formatted sql
-- changeset albina:009_000300 failOnError:true

ALTER TABLE regions DROP COLUMN SIMPLE_HTML_TEMPLATE_NAME;
