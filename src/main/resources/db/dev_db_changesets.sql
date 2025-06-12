-- liquibase formatted sql

-- changeset christina:1749713683909-33
DROP TABLE generic_observations;

--alter tables that should have varchar(191) instead of varchar(255) according to the previous liquibase changesets

-- changeset christina:1749713683909-7
ALTER TABLE regions MODIFY EMAIL_COLOR VARCHAR(191);

-- changeset christina:1749713683909-8
ALTER TABLE regions MODIFY GEO_DATA_DIRECTORY VARCHAR(191);

-- changeset christina:1749713683909-9
ALTER TABLE danger_source_variants MODIFY ID VARCHAR(191);

-- changeset christina:1749713683909-10
ALTER TABLE danger_sources MODIFY ID VARCHAR(191);

-- changeset christina:1749713683909-11
ALTER TABLE regions MODIFY IMAGE_COLORBAR_BW_PATH VARCHAR(191);

-- changeset christina:1749713683909-12
ALTER TABLE regions MODIFY IMAGE_COLORBAR_COLOR_PATH VARCHAR(191);

-- changeset christina:1749713683909-13
ALTER TABLE regions MODIFY MAP_LOGO_BW_PATH VARCHAR(191);

-- changeset christina:1749713683909-14
ALTER TABLE regions MODIFY MAP_LOGO_COLOR_PATH VARCHAR(191);

-- changeset christina:1749713683909-15
ALTER TABLE regions MODIFY MAP_LOGO_POSITION VARCHAR(191);

-- changeset christina:1749713683909-16
ALTER TABLE regions MODIFY PDF_COLOR VARCHAR(191);

-- changeset christina:1749713683909-17
ALTER TABLE regions MODIFY PDF_FOOTER_LOGO_BW_PATH VARCHAR(191);

-- changeset christina:1749713683909-18
ALTER TABLE regions MODIFY PDF_FOOTER_LOGO_COLOR_PATH VARCHAR(191);

-- changeset christina:1749713683909-19
ALTER TABLE regions MODIFY SIMPLE_HTML_TEMPLATE_NAME VARCHAR(191);

-- changeset christina:1749713683909-20
ALTER TABLE avalanche_problems MODIFY SNOWPACK_STABILITY VARCHAR(191);
