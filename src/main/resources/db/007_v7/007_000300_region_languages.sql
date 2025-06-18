-- liquibase formatted sql

-- changeset albina:007_000300-1
ALTER TABLE regions ADD ENABLED_LANGUAGES
    SET('de', 'it', 'en', 'fr', 'es', 'ca', 'oc')
    DEFAULT 'de,it,en,fr,es,ca,oc';

