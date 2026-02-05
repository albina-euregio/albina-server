-- liquibase formatted sql
-- changeset albina:008_000080 failOnError:true

ALTER TABLE danger_source_variants
    ADD TITLE varchar(255) NULL;
