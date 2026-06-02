-- liquibase formatted sql
-- changeset albina:008_002000 failOnError:true

ALTER TABLE regions ADD EDUCATION_URL VARCHAR(191) NULL;
