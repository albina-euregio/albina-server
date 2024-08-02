-- liquibase formatted sql
-- changeset albina:000_000030 failOnError:true

ALTER TABLE avalanche_bulletins ADD STRATEGIC_MINDSET varchar(191) NULL;
ALTER TABLE regions ADD ENABLE_STRATEGIC_MINDSET bit DEFAULT 0 NULL;
