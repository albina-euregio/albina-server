-- liquibase formatted sql
-- changeset legacy:000_000250 failOnError:true
-- comment create table rapid_mail_configurations
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'rapid_mail_configurations' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE rapid_mail_configurations (
    ID bigint NOT NULL AUTO_INCREMENT,
    PASSWORD varchar(191),
    USERNAME varchar(191),
    REGION_ID varchar(191),
    PRIMARY KEY (ID),
    CONSTRAINT FK_RAPID_MAIL_CONFIGURATIONS_REGIONS FOREIGN KEY (REGION_ID) REFERENCES regions (ID)
);

