-- liquibase formatted sql
-- changeset albina:008_000800 failOnError:true

CREATE TABLE avalanche_bulletin_photos
(
    ID                    varchar(191) NOT NULL,
    AVALANCHE_BULLETIN_ID varchar(191) NOT NULL,
    URL                   varchar(191) NOT NULL,
    COPYRIGHT             varchar(191) DEFAULT NULL,
    DATE                  date         DEFAULT NULL,
    MICROREGION_ID        varchar(191) DEFAULT NULL,
    LOCATION_NAME         varchar(191) DEFAULT NULL,
    LATITUDE              double       DEFAULT NULL,
    LONGITUDE             double       DEFAULT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT avalanche_bulletin_photos_avalanche_bulletins_FK FOREIGN KEY (AVALANCHE_BULLETIN_ID) REFERENCES avalanche_bulletins (ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT avalanche_bulletin_photos_bulletin_url_UK UNIQUE (AVALANCHE_BULLETIN_ID, URL)
);

CREATE INDEX avalanche_bulletin_photos_avalanche_bulletin_idx ON avalanche_bulletin_photos (AVALANCHE_BULLETIN_ID);
