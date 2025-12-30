-- liquibase formatted sql
-- changeset albina:008_000040 failOnError:true

ALTER TABLE danger_source_variants
    ADD UNCERTAINTY longtext NULL;
