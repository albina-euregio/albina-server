-- liquibase formatted sql
-- changeset albina:000_000290 failOnError:true
-- comment create table telegram_configurations
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'telegram_configurations' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE telegram_configurations (
    ID bigint NOT NULL AUTO_INCREMENT,
    API_TOKEN varchar(191),
    CHAT_ID varchar(191),
    LANGUAGE_CODE varchar(191),
    REGION_ID varchar(191),
    PRIMARY KEY (ID),
    CONSTRAINT FK_TELEGRAM_CONFIGURATIONS_REGIONS FOREIGN KEY (REGION_ID) REFERENCES regions (ID)
);

