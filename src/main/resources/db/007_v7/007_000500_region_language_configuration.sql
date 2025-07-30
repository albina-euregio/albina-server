-- liquibase formatted sql

-- changeset albina:007_000500-1
ALTER TABLE regions
    ADD COAT_OF_ARMS LONGBLOB NULL;

-- changeset albina:007_000500-2
ALTER TABLE regions
    ADD DEFAULT_LANG VARCHAR(191) NULL;

-- changeset albina:007_000500-3
ALTER TABLE regions
    ADD LOGO LONGBLOB NULL;

-- changeset albina:007_000500-4
ALTER TABLE regions
    ADD LOGO_BW LONGBLOB NULL;

-- changeset albina:007_000500-5
ALTER TABLE regions
    ADD LOGO_SECONDARY LONGBLOB NULL;

-- changeset albina:007_000500-6
ALTER TABLE regions
    ADD LOGO_SECONDARY_BW LONGBLOB NULL;

-- changeset albina:007_000500-7
CREATE TABLE region_language_configurations
(
    REGION_ID             VARCHAR(191) NOT NULL,
    LANGUAGE_CODE         VARCHAR(191) NOT NULL,
    STATIC_URL            VARCHAR(191) NULL,
    URL                   VARCHAR(191) NULL,
    URL_WITH_DATE         VARCHAR(191) NULL,
    WARNING_SERVICE_EMAIL VARCHAR(191) NULL,
    WARNING_SERVICE_NAME  VARCHAR(191) NULL,
    WEBSITE_NAME          VARCHAR(191) NULL,
    CONSTRAINT region_language_configurationsPK PRIMARY KEY (LANGUAGE_CODE, REGION_ID)
);

-- changeset albina:007_000500-8
ALTER TABLE region_language_configurations
    ADD CONSTRAINT FK_REGION_LANGUAGE_CONFIGURATION FOREIGN KEY (REGION_ID) REFERENCES regions (ID);
