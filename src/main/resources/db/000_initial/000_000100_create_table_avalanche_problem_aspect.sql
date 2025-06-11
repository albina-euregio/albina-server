-- liquibase formatted sql
-- changeset legacy:000_000100 failOnError:true
-- comment create table avalanche_problem_aspects
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'avalanche_problem_aspects' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA()) 
CREATE TABLE avalanche_problem_aspects (
    AVALANCHE_PROBLEM_ID varchar(191) NOT NULL,
    ASPECT integer,
    CONSTRAINT FK_AVALANCHE_PROBLEM_ASPECT_AVALANCHE_PROBLEM FOREIGN KEY (AVALANCHE_PROBLEM_ID) REFERENCES avalanche_problems (ID)
);

