-- liquibase formatted sql
-- changeset albina:007_000060 failOnError:true

ALTER TABLE danger_source_variants
    CHANGE COLUMN REMOTE_TRIGGERING REMOTE_TRIGGERING_OLD BIT(1);

ALTER TABLE danger_source_variants
    ADD COLUMN REMOTE_TRIGGERING ENUM ('likely','possible','unlikely');

UPDATE danger_source_variants
    SET REMOTE_TRIGGERING = IF(REMOTE_TRIGGERING_OLD, 'likely', 'unlikely');

ALTER TABLE danger_source_variants
    DROP COLUMN REMOTE_TRIGGERING_OLD;
