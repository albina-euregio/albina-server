-- liquibase formatted sql
-- changeset albina:000_000200 failOnError:true
-- comment create table avalanche_reports
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'avalanche_reports' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE avalanche_reports (
    ID varchar(191) NOT NULL,
    CAAML_V5_CREATED bit DEFAULT FALSE,
    CAAML_V6_CREATED bit DEFAULT FALSE,
    DATE datetime,
    EMAIL_CREATED bit DEFAULT FALSE,
    HTML_CREATED bit DEFAULT FALSE,
    JSON_CREATED bit DEFAULT FALSE,
    JSON_STRING longtext,
    MAP_CREATED bit DEFAULT FALSE,
    MEDIA_FILE_UPLOADED bit DEFAULT FALSE,
    PDF_CREATED bit DEFAULT FALSE,
    PUSH_SENT bit DEFAULT FALSE,
    STATUS integer,
    TELEGRAM_SENT bit DEFAULT FALSE,
    TIMESTAMP datetime,
    REGION_ID varchar(191),
    USER_ID varchar(191),
    PRIMARY KEY (ID),
    CONSTRAINT FK_AVALANCHE_REPORTS_REGIONS FOREIGN KEY (REGION_ID) REFERENCES regions (ID),
    CONSTRAINT FK_AVALANCHE_REPORTS_USERS FOREIGN KEY (USER_ID) REFERENCES users (EMAIL),
    INDEX avalanche_reports_DATE_IDX (date)
);

