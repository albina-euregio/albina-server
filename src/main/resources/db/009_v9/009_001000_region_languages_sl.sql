-- liquibase formatted sql
-- changeset albina:009_001000 failOnError:true
-- comment add Slovenian (sl) language

ALTER TABLE regions MODIFY ENABLED_LANGUAGES
    SET('de', 'it', 'en', 'fr', 'es', 'ca', 'oc', 'sl')
    DEFAULT 'de,it,en,fr,es,ca,oc';

ALTER TABLE regions MODIFY TTS_LANGUAGES
    SET('de', 'it', 'en', 'fr', 'es', 'ca', 'oc', 'sl')
    DEFAULT 'de,it,en,es,ca';
