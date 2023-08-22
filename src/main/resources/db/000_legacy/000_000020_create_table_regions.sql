-- liquibase formatted sql
-- changeset legacy:000_000020 failOnError:true
-- comment create table regions
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'regions' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE regions (
    ID varchar(191) NOT NULL COMMENT 'Primary Key',
    CREATE_CAAML_V5 bit DEFAULT FALSE COMMENT 'Should a CAAML 5 be generated when publishing or updating?',
    CREATE_CAAML_V6 bit DEFAULT FALSE COMMENT 'Should a CAAML 6 be generated when publishing or updating?',
    CREATE_JSON bit DEFAULT FALSE COMMENT 'Should a JSON be generated when publishing or updating?',
    CREATE_MAPS bit DEFAULT FALSE COMMENT 'Should a map, simple HTML and PDF be generated when publishing or updating?',
    CREATE_PDF bit DEFAULT FALSE COMMENT 'Should a PDF be generated when publishing or updating? CREATE_MAPS must be true for this.',
    CREATE_SIMPLE_HTML bit DEFAULT FALSE COMMENT 'Should a simple HTML bulletin be generated when publishing or updating? CREATE_MAPS must be true for this.',
    EMAIL_COLOR varchar(191),
    ENABLE_AVALANCHE_PROBLEM_CORNICES bit DEFAULT FALSE,
    ENABLE_AVALANCHE_PROBLEM_NO_DISTINCT_AVALANCHE_PROBLEM bit DEFAULT FALSE,
    ENABLE_MEDIA_FILE bit DEFAULT FALSE,
    GEO_DATA_DIRECTORY varchar(191) COMMENT 'Subdirectory in server_instance.MAP_PRODUCTION_URL in which the geo data regions are located.',
    IMAGE_COLORBAR_BW_PATH varchar(191),
    IMAGE_COLORBAR_COLOR_PATH varchar(191),
    MAP_CENTER_LAT double precision,
    MAP_CENTER_LNG double precision,
    MAP_LOGO_BW_PATH varchar(191),
    MAP_LOGO_COLOR_PATH varchar(191),
    MAP_LOGO_POSITION varchar(191),
    MAP_X_MAX integer,
    MAP_X_MIN integer,
    MAP_Y_MAX integer,
    MAP_Y_MIN integer,
    MICRO_REGIONS integer,
    PDF_COLOR varchar(191),
    PDF_FOOTER_LOGO bit DEFAULT FALSE,
    PDF_FOOTER_LOGO_BW_PATH varchar(191),
    PDF_FOOTER_LOGO_COLOR_PATH varchar(191),
    PDF_MAP_HEIGHT integer,
    PDF_MAP_WIDTH_AM_PM integer,
    PDF_MAP_WIDTH_FD integer,
    PDF_MAP_Y_AM_PM integer,
    PDF_MAP_Y_FD integer,
    PUBLISH_BLOGS bit DEFAULT FALSE,
    PUBLISH_BULLETINS bit DEFAULT FALSE,
    SEND_EMAILS bit DEFAULT FALSE COMMENT 'Should e-mails be sent when publishing or updating?',
    SEND_PUSH_NOTIFICATIONS bit DEFAULT FALSE COMMENT 'Should newsletters be sent by push when publishing or updating the bulletin? A corresponding push configuration must exist.',
    SEND_TELEGRAM_MESSAGES bit DEFAULT FALSE COMMENT 'Should a Telegram message be sent when publishing or updating? A corresponding Telegram configuration must exist.',
    SHOW_MATRIX bit DEFAULT FALSE COMMENT 'Show the EAWS matrix in the assessment.',
    SIMPLE_HTML_TEMPLATE_NAME varchar(191),
    SERVER_INSTANCE_ID bigint COMMENT 'Server Instance to which the region belongs.',
    PRIMARY KEY (ID),
    CONSTRAINT FK_REGION_SERVER_INSTANZ FOREIGN KEY (SERVER_INSTANCE_ID) REFERENCES server_instances (ID)
);
