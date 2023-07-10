-- liquibase formatted sql
-- changeset legacy:000_000150 failOnError:true
-- comment create table avalanche_bulletin_texts
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'avalanche_bulletin_texts' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE avalanche_bulletin_texts (
    AVALANCHE_BULLETIN_ID varchar(191) NOT NULL,
    TEXTS_ID varchar(191) NOT NULL,
    TEXT_TYPE varchar(191) NOT NULL,
    PRIMARY KEY (AVALANCHE_BULLETIN_ID, TEXT_TYPE),
    CONSTRAINT UK_AVALANCHE_BULLETIN_TEXTS_TEXTS_ID UNIQUE (TEXTS_ID),
    CONSTRAINT FK_AVALANCHE_BULLETIN_TEXTS_TEXTS FOREIGN KEY (TEXTS_ID) REFERENCES texts (ID),
    CONSTRAINT FK_AVALANCHE_BULLETIN_TEXTS_AVALANCHE_BULLETINS FOREIGN KEY (AVALANCHE_BULLETIN_ID) REFERENCES avalanche_bulletins (ID)
);

