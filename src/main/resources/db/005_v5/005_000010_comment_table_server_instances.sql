-- liquibase formatted sql
-- changeset albina:005_000010 failOnError:true
-- comment add comments to table server_instances
ALTER TABLE server_instances
    MODIFY COLUMN ID bigint NOT NULL AUTO_INCREMENT
      COMMENT 'Primary Key',
    MODIFY COLUMN API_URL varchar(191)
      COMMENT 'API URL of the server instance. Mandatory for external instances.',
    MODIFY COLUMN EXTERNAL_SERVER bit DEFAULT FALSE
      COMMENT 'Indicates whether it is a local (false) or external instance (true).',
    MODIFY COLUMN HTML_DIRECTORY varchar(191)
      COMMENT 'Root (output) directory for the Simple HTML Bulletin',
    MODIFY COLUMN MAP_PRODUCTION_URL varchar(191)
      COMMENT 'This directory contains the avalanche warning maps (regions)',
    MODIFY COLUMN MAPS_PATH varchar(191)
      COMMENT 'Root (output) directory for "Mapyrus" maps',
    MODIFY COLUMN MEDIA_PATH varchar(191)
      COMMENT 'Root directory for storing uploaded media files, also used in the RSS feed.',
    MODIFY COLUMN NAME varchar(191)
      COMMENT 'Display name of the instance and login name for login to the external server instance.',
    MODIFY COLUMN PASSWORD varchar(191)
      COMMENT 'Password for login to the external server instance.',
    MODIFY COLUMN PDF_DIRECTORY varchar(191)
      COMMENT 'Root (output) directory for CAAML, JSON and PDF.',
    MODIFY COLUMN PUBLISH_AT_5PM bit DEFAULT FALSE
      COMMENT 'If true, the automatic 5pm publish job runs. Concerns Locale Server instance.',
    MODIFY COLUMN PUBLISH_AT_8PM bit DEFAULT FALSE
      COMMENT 'If true, the automatic 8 pm update job runs. Concerns Locale Server instance.',
    MODIFY COLUMN SERVER_IMAGES_URL varchar(191)
      COMMENT 'URL via which the static bulletin images can be retrieved. Used in the HTML bulletin and in e-mails.',
    MODIFY COLUMN USER_NAME varchar(191)
      COMMENT 'Publication user. Must be filled in and also exist in the user table. Concerns Locale Server Instance',
    COMMENT 'Configuration of the server instances, may only contain one local instance.';
