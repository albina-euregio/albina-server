-- liquibase formatted sql

-- changeset albina:007_000510-1
ALTER TABLE regions ADD STATIC_URL VARCHAR(191) DEFAULT 'https://static.avalanche.report';

-- changeset albina:007_000510-2
ALTER TABLE region_language_configurations DROP COLUMN STATIC_URL;
