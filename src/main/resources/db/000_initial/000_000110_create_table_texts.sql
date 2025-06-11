-- liquibase formatted sql
-- changeset legacy:000_000110 failOnError:true
-- comment create table texts
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'texts' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE texts (
    ID varchar(191) NOT NULL,
    PRIMARY KEY (ID)
);

