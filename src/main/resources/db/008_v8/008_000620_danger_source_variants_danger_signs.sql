-- liquibase formatted sql
-- changeset albina:008_000620 failOnError:true

ALTER TABLE danger_source_variants
    ADD COLUMN DANGER_SIGNS set ('shooting_cracks', 'whumpfing', 'fresh_avalanches', 'glide_cracks');

UPDATE danger_source_variants
SET DANGER_SIGNS =
        (select GROUP_CONCAT(
                        case DANGER_SIGN
                            when 0 then 'shooting_cracks'
                            when 1 then 'whumpfing'
                            when 2 then 'fresh_avalanches'
                            when 3 then 'glide_cracks' end)
         from danger_source_variant_danger_signs
         where danger_source_variants.ID =
               danger_source_variant_danger_signs.DANGER_SOURCE_VARIANT_ID);

DROP TABLE danger_source_variant_danger_signs;
