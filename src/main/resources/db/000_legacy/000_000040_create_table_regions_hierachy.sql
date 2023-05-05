-- liquibase formatted sql
-- changeset legacy:000_000040 failOnError:true
-- comment create table region_hierarchy
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'region_hierarchy' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE region_hierarchy (
    SUB_REGION_ID varchar(191) NOT NULL,
    SUPER_REGION_ID varchar(191) NOT NULL,
    PRIMARY KEY (SUPER_REGION_ID, SUB_REGION_ID),
    CONSTRAINT FK_REGION_HIERACH_SUPER_REGION FOREIGN KEY (SUPER_REGION_ID) REFERENCES regions (ID),
    CONSTRAINT FK_REGION_HIERACH_SUP_REGION FOREIGN KEY (SUB_REGION_ID) REFERENCES regions (ID)
);

