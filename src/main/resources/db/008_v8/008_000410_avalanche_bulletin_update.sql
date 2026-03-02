-- liquibase formatted sql
-- changeset albina:008_000410 failOnError:true

ALTER TABLE avalanche_bulletins CHANGE COLUMN SAVE_DATE UPDATE_DATE datetime;
