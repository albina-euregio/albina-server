-- liquibase formatted sql
-- changeset albina:008_000900 failOnError:true

UPDATE avalanche_reports SET STATUS = 'draft' WHERE STATUS IS NULL;

ALTER TABLE avalanche_reports MODIFY STATUS enum ('republished', 'resubmitted', 'updated', 'published', 'submitted', 'draft', 'missing') NOT NULL;
