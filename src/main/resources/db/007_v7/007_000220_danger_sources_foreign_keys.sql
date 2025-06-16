-- liquibase formatted sql

-- changeset albina:007_000220-1
ALTER TABLE danger_source_variant_regions ADD CONSTRAINT FK3h4cnx2rft0jbndrnkhtq8onc FOREIGN KEY (DANGER_SOURCE_VARIANT_ID) REFERENCES danger_source_variants (ID);

-- changeset albina:007_000220-2
ALTER TABLE danger_source_variant_terrain_types ADD CONSTRAINT FK4e7cd53qw8279cfmh061gl1lc FOREIGN KEY (DANGER_SOURCE_VARIANT_ID) REFERENCES danger_source_variants (ID);

-- changeset albina:007_000220-3
ALTER TABLE danger_source_variant_aspects ADD CONSTRAINT FKfg2xat38uou4jpra0qfk6494 FOREIGN KEY (DANGER_SOURCE_VARIANT_ID) REFERENCES danger_source_variants (ID);

-- changeset albina:007_000220-4
ALTER TABLE danger_source_variants ADD CONSTRAINT FKn8vbooq7hbxwxix8tnkfce1bl FOREIGN KEY (DANGER_SOURCE_ID) REFERENCES danger_sources (ID);

-- changeset albina:007_000220-5
ALTER TABLE danger_source_variant_danger_signs ADD CONSTRAINT FKrwd0j6hkv0y5x8lx4ica2ge60 FOREIGN KEY (DANGER_SOURCE_VARIANT_ID) REFERENCES danger_source_variants (ID);
