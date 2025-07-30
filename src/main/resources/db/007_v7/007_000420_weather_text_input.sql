-- liquibase formatted sql

-- changeset albina:007_000420-1
ALTER TABLE regions ADD ENABLE_WEATHER_TEXT_FIELD bit(1) DEFAULT 0;

-- changeset albina:007_000420-2
ALTER TABLE avalanche_bulletins ADD SYNOPSIS_COMMENT_TEXTCAT LONGTEXT NULL;
