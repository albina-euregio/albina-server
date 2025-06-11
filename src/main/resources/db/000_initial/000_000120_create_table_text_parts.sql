-- liquibase formatted sql
-- changeset legacy:000_000120 failOnError:true
-- comment create table text_parts
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'text_parts' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE text_parts (
    TEXTS_ID varchar(191) NOT NULL,
    LANGUAGE_CODE varchar(191),
    text longtext,
    CONSTRAINT FK_TEXT_PARTS_TEXTS FOREIGN KEY (TEXTS_ID) REFERENCES texts (ID)
);

