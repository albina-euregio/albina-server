-- liquibase formatted sql
-- changeset albina:008_000700 failOnError:true

alter table regions
    modify ENABLED_EDITABLE_FIELDS set ('avActivityComment', 'avActivityHighlights', 'generalHeadlineComment', 'highlights', 'snowpackStructureComment', 'snowpackStructureHighlights', 'synopsisComment', 'synopsisHighlights', 'tendencyComment', 'travelAdvisoryComment', 'travelAdvisoryHighlights') default '' null;

alter table regions
    add ENABLED_TEXTCAT_FIELDS set ('avActivityComment', 'avActivityHighlights', 'generalHeadlineComment', 'highlights', 'snowpackStructureComment', 'snowpackStructureHighlights', 'synopsisComment', 'synopsisHighlights', 'tendencyComment', 'travelAdvisoryComment', 'travelAdvisoryHighlights') default 'highlights,avActivityHighlights,avActivityComment,snowpackStructureHighlights,snowpackStructureComment,tendencyComment' null;
