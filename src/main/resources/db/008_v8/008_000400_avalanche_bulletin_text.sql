-- liquibase formatted sql
-- changeset albina:008_000400 failOnError:true

CREATE TABLE avalanche_bulletin_texts_2026
(
    AVALANCHE_BULLETIN_ID varchar(191)                                                                                                                                                                                                                                                NOT NULL,
    TEXT_TYPE             set ('highlights','synopsisHighlights','synopsisComment','avActivityHighlights','avActivityComment','snowpackStructureHighlights','snowpackStructureComment','travelAdvisoryHighlights','travelAdvisoryComment','tendencyComment','generalHeadlineComment') NOT NULL,
    LANGUAGE_CODE         varchar(191)                                                                                                                                                                                                                                                NOT NULL,
    TEXT                  longtext DEFAULT NULL,
    PRIMARY KEY (AVALANCHE_BULLETIN_ID, TEXT_TYPE, LANGUAGE_CODE),
    CONSTRAINT avalanche_bulletin_texts_avalanche_bulletins_FK FOREIGN KEY (AVALANCHE_BULLETIN_ID) REFERENCES avalanche_bulletins (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO avalanche_bulletin_texts_2026
SELECT avalanche_bulletin_texts.AVALANCHE_BULLETIN_ID,
       avalanche_bulletin_texts.TEXT_TYPE,
       text_parts.LANGUAGE_CODE,
       text_parts.TEXT
FROM avalanche_bulletin_texts
JOIN text_parts ON avalanche_bulletin_texts.TEXTS_ID = text_parts.TEXTS_ID;

ALTER TABLE avalanche_bulletin_texts DROP FOREIGN KEY FKfr6qmtqtki9hj78bm72gtt8bp;
ALTER TABLE avalanche_bulletin_texts DROP FOREIGN KEY FKrrg7cyd64c8i2brbfnai6jc7v;
DROP TABLE avalanche_bulletin_texts;

ALTER TABLE text_parts DROP FOREIGN KEY FKfynmdlr1up76i0oe06jixe48t;
DROP TABLE text_parts;

DROP TABLE texts;

RENAME TABLE avalanche_bulletin_texts_2026 TO avalanche_bulletin_texts;
