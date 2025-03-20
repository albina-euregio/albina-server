-- liquibase formatted sql
-- changeset albina:000_000050 failOnError:true
CREATE TABLE danger_source_variant_texts (
    ID MEDIUMINT NOT NULL AUTO_INCREMENT,
    AVALANCHE_TYPE enum ('glide','loose','slab'),
    AVALANCHE_PROBLEM varchar(191),
    HAS_DAYTIME_DEPENDENCY bit, 
    GLIDING_SNOW_ACTIVITY enum ('high','low','medium'), 
    AVALANCHE_SIZE varchar(191),
    FREQUENCY varchar(191),
    SNOWPACK_STABILITY varchar(191),
    TEXTCAT longtext,
    PRIMARY KEY (ID)
) engine=InnoDB;
