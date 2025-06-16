-- liquibase formatted sql
-- changeset albina:000_000210 failOnError:true
-- comment create table chat_messages
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'chat_messages' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE chat_messages (
    ID varchar(191) NOT NULL,
    CHAT_ID integer,
    DATETIME datetime,
    TEXT varchar(191),
    USERNAME varchar(191),
    PRIMARY KEY (ID)
);

