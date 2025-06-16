-- liquibase formatted sql
-- changeset albina:000_000010 failOnError:true
-- comment create table server_instances
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'server_instances' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE server_instances (
    ID bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    API_URL varchar(191) COMMENT 'API URL of the server instance. Mandatory for external instances.',
    EXTERNAL_SERVER bit DEFAULT FALSE COMMENT 'Indicates whether it is a local (false) or external instance (true).',
    HTML_DIRECTORY varchar(191) COMMENT 'Root (output) directory for the Simple HTML Bulletin',
    MAP_PRODUCTION_URL varchar(191) COMMENT 'This directory contains the avalanche warning maps (regions)',
    MAPS_PATH varchar(191) COMMENT 'Root (output) directory for "Mapyrus" maps',
    MEDIA_PATH varchar(191) COMMENT 'Root directory for storing uploaded media files, also used in the RSS feed.',
    NAME varchar(191) COMMENT 'Display name of the instance and login name for login to the external server instance.',
    PASSWORD varchar(191) COMMENT 'Password for login to the external server instance.',
    PDF_DIRECTORY varchar(191) COMMENT 'Root (output) directory for CAAML, JSON and PDF.',
    PUBLISH_AT_5PM bit DEFAULT FALSE COMMENT 'If true, the automatic 5pm publish job runs. Concerns Locale Server instance.',
    PUBLISH_AT_8PM bit DEFAULT FALSE COMMENT 'If true, the automatic 8 pm update job runs. Concerns Locale Server instance.',
    SERVER_IMAGES_URL varchar(191) COMMENT 'URL via which the static bulletin images can be retrieved. Used in the HTML bulletin and in e-mails.',
    USER_NAME varchar(191) COMMENT 'Publication user. Must be filled in and also exist in the user table. Concerns Locale Server Instance',
    PRIMARY KEY (ID));

