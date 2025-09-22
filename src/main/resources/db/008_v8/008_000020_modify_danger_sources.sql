-- liquibase formatted sql
-- changeset albina:008_000020 failOnError:true

ALTER TABLE danger_sources
    ADD OWNER_REGION VARCHAR(191) DEFAULT 'AT-07';
