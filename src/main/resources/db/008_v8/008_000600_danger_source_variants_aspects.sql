-- liquibase formatted sql
-- changeset albina:008_000600 failOnError:true

ALTER TABLE danger_source_variants
    ADD COLUMN ASPECTS set ('N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW');

UPDATE danger_source_variants
SET ASPECTS =
        (select GROUP_CONCAT(
                        case aspect
                            when 0 then 'N'
                            when 1 then 'NE'
                            when 2 then 'E'
                            when 3 then 'SE'
                            when 4 then 'S'
                            when 5 then 'SW'
                            when 6 then 'W'
                            when 7 then 'NW' end)
         from danger_source_variant_aspects
         where danger_source_variants.ID =
               danger_source_variant_aspects.DANGER_SOURCE_VARIANT_ID);

DROP TABLE danger_source_variant_aspects;
