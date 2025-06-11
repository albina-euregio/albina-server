-- liquibase formatted sql
-- changeset legacy:000_000260 failOnError:true
-- comment create table push_configurations
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'push_configurations' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE push_configurations (
    ID bigint NOT NULL AUTO_INCREMENT,
    VAPID_PRIVATE_KEY varchar(191),
    VAPID_PUBLIC_KEY varchar(191),
    PRIMARY KEY (ID)
);

