-- liquibase formatted sql
-- changeset albina:000_000050 failOnError:true

ALTER TABLE users ADD LANGUAGE_CODE varchar(191) NULL;
ALTER TABLE users ADD DELETED BIT DEFAULT 0;
