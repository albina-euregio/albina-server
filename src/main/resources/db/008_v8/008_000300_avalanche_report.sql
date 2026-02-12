-- liquibase formatted sql
-- changeset albina:008_000300 failOnError:true

ALTER TABLE avalanche_reports DROP FOREIGN KEY IF EXISTS FKajgejvjpourfacpfjn6ndlb6c;

ALTER TABLE avalanche_reports DROP COLUMN USER_ID;
