-- liquibase formatted sql
-- changeset legacy:000_000270 failOnError:true
-- comment create table push_subscriptions
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'push_subscriptions' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE push_subscriptions (
    ID bigint NOT NULL AUTO_INCREMENT,
    AUTH varchar(191),
    ENDPOINT longtext,
    FAILED_COUNT integer,
    LANGUAGE_CODE varchar(191),
    P256DH varchar(191),
    REGION_ID varchar(191),
    SUBSCRIBE_DATE datetime,
    PRIMARY KEY (ID)
);

