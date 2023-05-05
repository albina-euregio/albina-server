-- liquibase formatted sql
-- changeset legacy:000_000070 failOnError:true
-- comment create table user_region
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'user_region' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE user_region (
    USER_EMAIL varchar(191) NOT NULL,
    REGION_ID varchar(191) NOT NULL,
    PRIMARY KEY (USER_EMAIL, REGION_ID),
    CONSTRAINT FK_USER_REGION_REGION FOREIGN KEY (REGION_ID) REFERENCES regions (ID),
    CONSTRAINT FK_USER_REGION_USER FOREIGN KEY (USER_EMAIL) REFERENCES users (EMAIL)
);

