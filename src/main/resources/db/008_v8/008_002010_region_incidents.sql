-- liquibase formatted sql
-- changeset albina:008_002010 failOnError:true

ALTER TABLE regions ADD ENABLE_INCIDENTS bit(1) DEFAULT 0;
