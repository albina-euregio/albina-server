-- liquibase formatted sql
-- changeset legacy:000_000280 failOnError:true
-- comment create table google_blogger_configurations
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'google_blogger_configurations' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE google_blogger_configurations (
    ID bigint NOT NULL AUTO_INCREMENT,
    API_KEY varchar(191),
    BLOG_API_URL varchar(191),
    BLOG_ID varchar(191),
    BLOG_URL varchar(191),
    LANGUAGE_CODE varchar(191),
    LAST_PUBLISHED_BLOG_ID varchar(255),
    LAST_PUBLISHED_TIMESTAMP datetime(6),
    REGION_ID varchar(191),
    PRIMARY KEY (ID),
    CONSTRAINT FK_GOOGLE_BLOGGER_CONFIGURATIONS_REGIONS FOREIGN KEY (REGION_ID) REFERENCES regions (ID)
);

