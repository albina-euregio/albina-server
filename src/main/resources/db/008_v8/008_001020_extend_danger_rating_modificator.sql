-- liquibase formatted sql
-- changeset albina:008_001020 failOnError:true

ALTER TABLE danger_source_variants MODIFY DANGER_RATING_MODIFICATOR enum ('none','minus','equal','plus');