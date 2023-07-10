-- liquibase formatted sql
-- changeset legacy:000_000080 failOnError:true
-- comment create table user_region_role_links
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'user_region_role_links' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE user_region_role_links (
    ID bigint NOT NULL AUTO_INCREMENT,
    ROLE varchar(191),
    REGION_ID varchar(191),
    USER_EMAIL varchar(191),
    PRIMARY KEY (ID),
    CONSTRAINT FK_USER_REGION_ROLE_LINK_REGIONS FOREIGN KEY (REGION_ID) REFERENCES regions (ID),
    CONSTRAINT FK_USER_REGION_ROLE_LINK_USERS FOREIGN KEY (USER_EMAIL) REFERENCES users (EMAIL)
);

