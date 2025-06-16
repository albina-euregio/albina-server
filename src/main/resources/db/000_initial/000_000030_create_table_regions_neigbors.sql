-- liquibase formatted sql
-- changeset albina:000_000030 failOnError:true
-- comment create table region_neighbors
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'region_neighbors' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE region_neighbors (
    REGION_ID varchar(191) NOT NULL,
    NEIGHBOR_REGION_ID varchar(191) NOT NULL,
    PRIMARY KEY (REGION_ID, NEIGHBOR_REGION_ID),
    CONSTRAINT FK_REGION_NEIGHBORS_REGION FOREIGN KEY (NEIGHBOR_REGION_ID) REFERENCES regions (ID),
    CONSTRAINT FK_REGION_NEIGHBORS_NEIGHBOR_REGION FOREIGN KEY (REGION_ID) REFERENCES regions (ID));

