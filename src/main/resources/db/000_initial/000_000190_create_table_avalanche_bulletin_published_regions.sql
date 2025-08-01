-- liquibase formatted sql
-- changeset albina:000_000190 failOnError:true
-- comment create table avalanche_bulletin_published_regions
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'avalanche_bulletin_published_regions' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE avalanche_bulletin_published_regions (
    AVALANCHE_BULLETIN_ID varchar(191) NOT NULL,
    REGION_ID varchar(191),
    CONSTRAINT FK_AVALANCHE_BULLETIN_PUBLISHED_REGIONS_AVALANCHE_BULLETINS FOREIGN KEY (AVALANCHE_BULLETIN_ID) REFERENCES avalanche_bulletins (ID)
);

