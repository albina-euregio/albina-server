-- liquibase formatted sql
-- changeset albina:008_000060 failOnError:true

ALTER TABLE danger_sources MODIFY COLUMN DESCRIPTION longtext NULL;
