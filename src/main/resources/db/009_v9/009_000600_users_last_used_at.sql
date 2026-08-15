-- liquibase formatted sql
-- changeset albina:009_000600 failOnError:true

ALTER TABLE users ADD LAST_USED_AT datetime(6) NULL;
