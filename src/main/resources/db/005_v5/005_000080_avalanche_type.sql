-- liquibase formatted sql
-- changeset albina:000_000030 failOnError:true

ALTER TABLE avalanche_problems ADD AVALANCHE_TYPE varchar(191) NULL;
