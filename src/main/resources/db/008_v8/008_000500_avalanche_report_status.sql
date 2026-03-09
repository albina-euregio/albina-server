-- liquibase formatted sql
-- changeset albina:008_000500 failOnError:true

ALTER TABLE avalanche_reports
    MODIFY COLUMN STATUS varchar(31);

UPDATE avalanche_reports
SET STATUS = CASE STATUS
                 WHEN '0' THEN 'republished'
                 WHEN '1' THEN 'resubmitted'
                 WHEN '2' THEN 'updated'
                 WHEN '3' THEN 'published'
                 WHEN '4' THEN 'submitted'
                 WHEN '5' THEN 'draft'
                 WHEN '6' THEN 'missing'
                 else null end;

ALTER TABLE avalanche_reports
    MODIFY COLUMN STATUS enum ('republished', 'resubmitted', 'updated', 'published', 'submitted', 'draft', 'missing');

