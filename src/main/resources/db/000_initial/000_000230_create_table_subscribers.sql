-- liquibase formatted sql
-- changeset albina:000_000230 failOnError:true
-- comment create table subscribers
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'subscribers' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE subscribers (
    EMAIL varchar(191) NOT NULL,
    CONFIRMED bit DEFAULT FALSE,
    LANGUAGE varchar(191),
    PDF_ATTACHMENT bit DEFAULT FALSE,
    PRIMARY KEY (EMAIL)
);

