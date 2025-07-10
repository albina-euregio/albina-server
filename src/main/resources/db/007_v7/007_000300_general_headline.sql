-- liquibase formatted sql

-- changeset albina:007_000300-1 failOnError:true
ALTER TABLE regions ADD ENABLE_GENERAL_HEADLINE bit(1) DEFAULT 0;

-- changeset albina:007_000300-2 failOnError:true
ALTER TABLE avalanche_bulletins ADD GENERAL_HEADLINE_COMMENT_NOTES LONGTEXT NULL;

-- changeset albina:007_000300-3 failOnError:true
ALTER TABLE avalanche_bulletins ADD GENERAL_HEADLINE_COMMENT_TEXTCAT LONGTEXT NULL;
