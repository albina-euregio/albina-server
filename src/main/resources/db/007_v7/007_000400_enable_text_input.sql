-- liquibase formatted sql

-- changeset albina:007_000400
ALTER TABLE regions ADD ENABLE_EDITABLE_FIELDS bit(1) DEFAULT 0;
