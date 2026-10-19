-- liquibase formatted sql

-- changeset albina:009_000700-1 failOnError:true
ALTER TABLE avalanche_bulletins ADD TRAVEL_ADVISORY_HIGHLIGHTS_TEXTCAT LONGTEXT NULL;

-- changeset albina:009_000700-2 failOnError:true
ALTER TABLE avalanche_bulletins ADD TRAVEL_ADVISORY_COMMENT_TEXTCAT LONGTEXT NULL;

-- changeset albina:009_000700-3 failOnError:true
ALTER TABLE avalanche_bulletins ADD TRAVEL_ADVISORY_HIGHLIGHTS_NOTES LONGTEXT NULL;

-- changeset albina:009_000700-4 failOnError:true
ALTER TABLE avalanche_bulletins ADD TRAVEL_ADVISORY_COMMENT_NOTES LONGTEXT NULL;
