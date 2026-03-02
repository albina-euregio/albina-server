-- liquibase formatted sql
-- changeset albina:008_000650 failOnError:true

ALTER TABLE avalanche_problems
    ADD COLUMN ASPECTS set ('N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW');

UPDATE avalanche_problems
SET ASPECTS =
        (select GROUP_CONCAT(
                        case ASPECT
                            when 0 then 'N'
                            when 1 then 'NE'
                            when 2 then 'E'
                            when 3 then 'SE'
                            when 4 then 'S'
                            when 5 then 'SW'
                            when 6 then 'W'
                            when 7 then 'NW' end)
         from avalanche_problem_aspects
         where avalanche_problems.ID =
               avalanche_problem_aspects.AVALANCHE_PROBLEM_ID);

DROP TABLE avalanche_problem_aspects;
