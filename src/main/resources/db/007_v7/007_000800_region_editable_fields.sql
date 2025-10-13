-- liquibase formatted sql

-- changeset albina:007_000800_region_editable_fields-1
ALTER TABLE regions
    ADD ENABLED_EDITABLE_FIELDS
        SET ('avActivityComment','avActivityHighlights','generalHeadlineComment','highlights','snowpackStructureComment','snowpackStructureHighlights','synopsisComment','tendencyComment')
        DEFAULT '';

-- changeset albina:007_000800_region_editable_fields-2
UPDATE regions
SET ENABLED_EDITABLE_FIELDS='avActivityComment,avActivityHighlights,generalHeadlineComment,highlights,snowpackStructureComment,snowpackStructureHighlights,synopsisComment,tendencyComment'
WHERE ENABLE_EDITABLE_FIELDS = 1;

-- changeset albina:007_000800_region_editable_fields-3
ALTER TABLE regions
    DROP ENABLE_EDITABLE_FIELDS;
