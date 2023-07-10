-- liquibase formatted sql
-- changeset legacy:000_000160 failOnError:true
-- comment create table avalanche_bulletin_additional_users
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'avalanche_bulletin_additional_users' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE avalanche_bulletin_additional_users (
    AVALANCHE_BULLETIN_ID varchar(191) NOT NULL,
    ADDITIONAL_USER_NAME varchar(191),
    CONSTRAINT FK_AVALANCHE_BULLETIN_ADDITONAL_USER_AVALANCHE_BULLETIN FOREIGN KEY (AVALANCHE_BULLETIN_ID) REFERENCES avalanche_bulletins (ID)
);

