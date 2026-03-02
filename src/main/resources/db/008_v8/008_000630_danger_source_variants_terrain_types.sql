-- liquibase formatted sql
-- changeset albina:008_000630 failOnError:true

ALTER TABLE danger_source_variants
    ADD COLUMN TERRAIN_TYPES set ('gullies_and_bowls', 'adjacent_to_ridgelines', 'distant_from_ridgelines', 'in_the_vicinity_of_peaks', 'pass_areas', 'shady_slopes', 'sunny_slopes', 'grassy_slopes', 'cut_slopes', 'wind_loaded_slopes', 'base_of_rock_walls', 'behind_abrupt_changes_in_the_terrain', 'transitions_into_gullies_and_bowls', 'areas_where_the_snow_cover_is_rather_shallow', 'transitions_from_a_shallow_to_a_deep_snowpack', 'highly_frequented_off_piste_terrain', 'little_used_backcountry_terrain', 'places_that_are_protected_from_the_wind', 'regions_exposed_to_the_foehn_wind', 'regions_with_a_lot_of_snow', 'regions_exposed_to_precipitation', 'regions_exposed_to_heavier_precipitation');

UPDATE danger_source_variants
SET TERRAIN_TYPES =
        (select GROUP_CONCAT(
                        case TERRAIN_TYPE
                            when 0 then 'gullies_and_bowls'
                            when 1 then 'adjacent_to_ridgelines'
                            when 2 then 'distant_from_ridgelines'
                            when 3 then 'in_the_vicinity_of_peaks'
                            when 4 then 'pass_areas'
                            when 5 then 'shady_slopes'
                            when 6 then 'sunny_slopes'
                            when 7 then 'grassy_slopes'
                            when 8 then 'cut_slopes'
                            when 9 then 'wind_loaded_slopes'
                            when 10 then 'base_of_rock_walls'
                            when 11 then 'behind_abrupt_changes_in_the_terrain'
                            when 12 then 'transitions_into_gullies_and_bowls'
                            when 13 then 'areas_where_the_snow_cover_is_rather_shallow'
                            when 14 then 'transitions_from_a_shallow_to_a_deep_snowpack'
                            when 15 then 'highly_frequented_off_piste_terrain'
                            when 16 then 'little_used_backcountry_terrain'
                            when 17 then 'places_that_are_protected_from_the_wind'
                            when 18 then 'regions_exposed_to_the_foehn_wind'
                            when 19 then 'regions_with_a_lot_of_snow'
                            when 20 then 'regions_exposed_to_precipitation'
                            when 21 then 'regions_exposed_to_heavier_precipitation'
                            end)
         from danger_source_variant_terrain_types
         where danger_source_variants.ID =
               danger_source_variant_terrain_types.DANGER_SOURCE_VARIANT_ID);

DROP TABLE danger_source_variant_terrain_types;
