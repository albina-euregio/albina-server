-- liquibase formatted sql
-- changeset albina:005_000020 failOnError:true
-- comment add comments to table regions
ALTER TABLE  regions
    MODIFY COLUMN ID varchar(191) NOT NULL
      COMMENT 'Primary Key',
    MODIFY COLUMN CREATE_CAAML_V5 bit DEFAULT FALSE
      COMMENT 'Should a CAAML 5 be generated when publishing or updating?',
    MODIFY COLUMN  CREATE_CAAML_V6 bit DEFAULT FALSE
      COMMENT 'Should a CAAML 6 be generated when publishing or updating?',
    MODIFY COLUMN CREATE_JSON bit DEFAULT FALSE
      COMMENT 'Should a JSON be generated when publishing or updating?',
    MODIFY COLUMN CREATE_MAPS bit DEFAULT FALSE
      COMMENT 'Should a map, simple HTML and PDF be generated when publishing or updating?',
    MODIFY COLUMN CREATE_PDF bit DEFAULT FALSE
      COMMENT 'Should a PDF be generated when publishing or updating? CREATE_MAPS must be true for this.',
    MODIFY COLUMN CREATE_SIMPLE_HTML bit DEFAULT FALSE
      COMMENT 'Should a simple HTML bulletin be generated when publishing or updating? CREATE_MAPS must be true for this.',
    MODIFY COLUMN  GEO_DATA_DIRECTORY varchar(191)
      COMMENT 'Subdirectory in server_instance.MAP_PRODUCTION_URL in which the geo data regions are located.',
    MODIFY COLUMN SEND_EMAILS bit DEFAULT FALSE
      COMMENT 'Should e-mails be sent when publishing or updating?',
    MODIFY COLUMN SEND_PUSH_NOTIFICATIONS bit DEFAULT FALSE
      COMMENT 'Should newsletters be sent by push when publishing or updating the bulletin? A corresponding push configuration must exist.',
    MODIFY COLUMN SEND_TELEGRAM_MESSAGES bit DEFAULT FALSE
      COMMENT 'Should a Telegram message be sent when publishing or updating? A corresponding Telegram configuration must exist.',
    MODIFY COLUMN SHOW_MATRIX bit DEFAULT FALSE
      COMMENT 'Show the EAWS matrix in the assessment.',
    MODIFY COLUMN SERVER_INSTANCE_ID bigint
      COMMENT 'Server Instance to which the region belongs.';

