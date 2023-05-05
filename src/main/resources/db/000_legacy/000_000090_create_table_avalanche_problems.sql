-- liquibase formatted sql
-- changeset legacy:000_000090 failOnError:true
-- comment create table avalanche_problems
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'avalanche_problems' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE avalanche_problems (
    ID varchar(191) NOT NULL,
    AVALANCHE_PROBLEM varchar(191),
    DANGER_RATING_DIRECTION varchar(191),
    AVALANCHE_SIZE varchar(191),
    AVALANCHE_SIZE_VALUE integer,
    DANGER_RATING varchar(191),
    DANGER_RATING_MODIFICATOR varchar(191),
    FREQUENCY varchar(191),
    FREQUENCY_VALUE integer,
    SNOWPACK_STABILITY varchar(191),
    SNOWPACK_STABILITY_VALUE integer,
    ELEVATION_HIGH integer,
    ELEVATION_LOW integer,
    ARTIFICIAL_AVALANCHE_RELEASE_PROBABILITY varchar(191),
    ARTIFICIAL_AVALANCHE_SIZE varchar(191),
    ARTIFICIAL_DANGER_RATING varchar(191),
    ARTIFICIAL_HAZARD_SITE_DISTRIBUTION varchar(191),
    NATURAL_AVALANCHE_RELEASE_PROBABILITY varchar(191),
    NATURAL_DANGER_RATING varchar(191),
    NATURAL_HAZARD_SITE_DISTRIBUTION varchar(191),
    TERRAIN_FEATURE_TEXTCAT longtext,
    TREELINE_HIGH bit DEFAULT FALSE,
    TREELINE_LOW bit DEFAULT FALSE,
    PRIMARY KEY (ID)
);

