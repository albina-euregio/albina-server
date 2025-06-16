-- liquibase formatted sql
-- changeset albina:000_000050 failOnError:true
-- comment create table users
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'users' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE users (
    EMAIL varchar(191) NOT NULL,
    IMAGE LONGBLOB,
    NAME varchar(191),
    ORGANIZATION varchar(191),
    PASSWORD varchar(191),
    PRIMARY KEY (EMAIL)
);

