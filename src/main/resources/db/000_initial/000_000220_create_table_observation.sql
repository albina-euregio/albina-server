-- liquibase formatted sql
-- changeset albina:000_000220 failOnError:true
-- comment create table observations
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'observations' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE observations (
    ID bigint NOT NULL AUTO_INCREMENT,
    ASPECT varchar(191),
    AUTHOR_NAME varchar(191),
    CONTENT longtext,
    ELEVATION double precision,
    EVENT_DATE datetime,
    EVENT_TYPE integer,
    LATITUDE double precision,
    LOCATION_NAME varchar(191),
    LONGITUDE double precision,
    REGION_ID varchar(191),
    REPORT_DATE datetime,
    PRIMARY KEY (ID)
);

