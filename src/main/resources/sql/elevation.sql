/* Add columns to daytime descriptions */
ALTER TABLE `ais`.`avalanche_bulletin_daytime_descriptions`
ADD `TREELINE` bit DEFAULT NULL,
ADD `HAS_ELEVATION_DEPENDENCY` bit DEFAULT NULL,
ADD `ELEVATION` integer DEFAULT NULL

/* Copy elevation from bulletin to daytime descriptions */
UPDATE `ais`.`avalanche_bulletin_daytime_descriptions` d
	INNER JOIN `ais`.`avalanche_bulletins` b
		ON b.`FORENOON_ID` = d.`ID`
SET d.`TREELINE` = b.`TREELINE`, d.`HAS_ELEVATION_DEPENDENCY` = b.`HAS_ELEVATION_DEPENDENCY`, d.`ELEVATION` = b.`ELEVATION`

UPDATE `ais`.`avalanche_bulletin_daytime_descriptions` d
	INNER JOIN `ais`.`avalanche_bulletins` b
		ON b.`AFTERNOON_ID` = d.`ID`
SET d.`TREELINE` = b.`TREELINE`, d.`HAS_ELEVATION_DEPENDENCY` = b.`HAS_ELEVATION_DEPENDENCY`, d.`ELEVATION` = b.`ELEVATION`

/* Remove columns from bulletin */
/*
ALTER TABLE `ais`.`avalanche_bulletins`
DROP COLUMN `TREELINE`,
DROP COLUMN `HAS_ELEVATION_DEPENDENCY`,
DROP COLUMN `ELEVATION`
*/