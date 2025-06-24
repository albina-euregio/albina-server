-- liquibase formatted sql
-- changeset albina:000_000240 failOnError:true
-- comment create table subscriber_regions
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'subscriber_regions' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE subscriber_regions (
    SUBSCRIBER_ID varchar(191) NOT NULL,
    REGION_ID varchar(191) NOT NULL,
    CONSTRAINT FK_SUBSCRIBER_REGIONS_REGIONS FOREIGN KEY (REGION_ID) REFERENCES regions (ID),
    CONSTRAINT FK_SUBSCRIBER_REGIONS_SUBSCRIBERS FOREIGN KEY (SUBSCRIBER_ID) REFERENCES subscribers (EMAIL)
);

