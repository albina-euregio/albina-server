-- liquibase formatted sql
-- changeset albina:006_000080 failOnError:true

ALTER TABLE avalanche_problems ADD AVALANCHE_TYPE varchar(191) NULL;
