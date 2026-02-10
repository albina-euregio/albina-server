-- liquibase formatted sql
-- changeset albina:008_000090 failOnError:true

ALTER TABLE avalanche_bulletins ADD SAVE_DATE datetime NULL;
