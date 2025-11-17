-- liquibase formatted sql
-- changeset albina:008_000030 failOnError:true

ALTER TABLE danger_source_variant_aspects DROP FOREIGN KEY FKfg2xat38uou4jpra0qfk6494;
ALTER TABLE danger_source_variant_aspects ADD CONSTRAINT FKfg2xat38uou4jpra0qfk6494 FOREIGN KEY (DANGER_SOURCE_VARIANT_ID) REFERENCES danger_source_variants(ID) ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE danger_source_variant_danger_signs DROP FOREIGN KEY FKrwd0j6hkv0y5x8lx4ica2ge60;
ALTER TABLE danger_source_variant_danger_signs ADD CONSTRAINT FKrwd0j6hkv0y5x8lx4ica2ge60 FOREIGN KEY (DANGER_SOURCE_VARIANT_ID) REFERENCES danger_source_variants(ID) ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE danger_source_variant_regions DROP FOREIGN KEY FK3h4cnx2rft0jbndrnkhtq8onc;
ALTER TABLE danger_source_variant_regions ADD CONSTRAINT FK3h4cnx2rft0jbndrnkhtq8onc FOREIGN KEY (DANGER_SOURCE_VARIANT_ID) REFERENCES danger_source_variants(ID) ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE danger_source_variant_terrain_types DROP FOREIGN KEY FK4e7cd53qw8279cfmh061gl1lc;
ALTER TABLE danger_source_variant_terrain_types ADD CONSTRAINT FK4e7cd53qw8279cfmh061gl1lc FOREIGN KEY (DANGER_SOURCE_VARIANT_ID) REFERENCES danger_source_variants(ID) ON DELETE CASCADE ON UPDATE RESTRICT;
