-- liquibase formatted sql
-- changeset albina:009_000300 failOnError:true

ALTER TABLE regions DROP COLUMN CREATE_CAAML_V5;
ALTER TABLE avalanche_reports DROP COLUMN CAAML_V5_CREATED;
