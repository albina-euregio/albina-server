ALTER TABLE `ais_tmp`.`avalanche_reports` CHANGE `CAAML_CREATED` `CAAML_V5_CREATED` bit;
ALTER TABLE `ais_tmp`.`avalanche_reports` ADD `CAAML_V6_CREATED` bit default false;

ALTER TABLE `ais_tmp`.`avalanche_reports` ADD `JSON_CREATED` bit default false;

ALTER TABLE `ais_tmp`.`avalanche_reports` DROP COLUMN `WHATSAPP_SENT`;
