/* Add column to avalanche reports */
ALTER TABLE `ais`.`avalanche_reports`
ADD `MEDIA_FILE_UPLOADED` bit DEFAULT FALSE

ALTER TABLE `ais`.`server_instances`
ADD `MEDIA_PATH` varchar(255);

ALTER TABLE `ais`.`regions`
ADD `ENABLE_MEDIA_FILE` bit DEFAULT FALSE
