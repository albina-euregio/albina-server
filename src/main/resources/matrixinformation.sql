ALTER TABLE `ais`.`avalanche_situation`
ADD `ARTIFICIAL_DANGER_RATING` varchar(255) DEFAULT NULL,
ADD `ARTIFICIAL_AVALANCHE_SIZE` varchar(255) DEFAULT NULL,
ADD `ARTIFICIAL_AVALANCHE_RELEASE_PROBABILITY` varchar(255) DEFAULT NULL,
ADD `ARTIFICIAL_HAZARD_SITE_DISTRIBUTION` varchar(255) DEFAULT NULL,
ADD `NATURAL_DANGER_RATING` varchar(255) DEFAULT NULL,
ADD `NATURAL_AVALANCHE_RELEASE_PROBABILITY` varchar(255) DEFAULT NULL,
ADD `NATURAL_HAZARD_SITE_DISTRIBUTION` varchar(255) DEFAULT NULL,
ADD `TERRAIN_FEATURE_TEXTCAT` longtext DEFAULT NULL

ALTER TABLE `ais`.`avalanche_bulletin_daytime_descriptions`
ADD `AVALANCHE_SITUATION_3_ID` varchar(255) DEFAULT NULL,
ADD `AVALANCHE_SITUATION_4_ID` varchar(255) DEFAULT NULL,
ADD `AVALANCHE_SITUATION_5_ID` varchar(255) DEFAULT NULL,
ADD `TERRAIN_FEATURE_ABOVE_TEXTCAT` longtext DEFAULT NULL,
ADD `TERRAIN_FEATURE_BELOW_TEXTCAT` longtext DEFAULT NULL

ALTER TABLE `ais`.`avalanche_bulletins`
ADD `HIGHLIGHTS_TEXTCAT` longtext DEFAULT NULL

ALTER TABLE `ais`.`avalanche_bulletin_daytime_descriptions`
ADD `COMPLEXITY` varchar(255) DEFAULT NULL

ALTER TABLE `ais`.`avalanche_bulletins`
ADD `IS_MANUAL_DANGER_RATING` bit(1) DEFAULT NULL
