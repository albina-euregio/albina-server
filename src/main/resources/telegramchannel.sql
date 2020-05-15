INSERT INTO `ais`.`socialmedia_provider` (`ID`, `NAME`) VALUES ('4', 'Telegram');
INSERT INTO `ais`.`socialmedia_channel` (`ID`, `NAME`, `PROVIDER_ID`) VALUES ('6', 'TelegramChannel', '4');

INSERT INTO `ais`.`socialmedia_channel_region` (`REGION_ID`, `CHANNEL_ID`) VALUES ('1', '6');
INSERT INTO `ais`.`socialmedia_channel_region` (`REGION_ID`, `CHANNEL_ID`) VALUES ('2', '6');
INSERT INTO `ais`.`socialmedia_channel_region` (`REGION_ID`, `CHANNEL_ID`) VALUES ('3', '6');

CREATE TABLE `socialmedia_telegram_config` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `API_TOKEN` varchar(255) DEFAULT NULL,
  `CHAT_ID` varchar(255) DEFAULT NULL,
  `LANGUAGE_CODE` varchar(255) DEFAULT NULL,  
  `PROVIDER_ID` bigint(20) DEFAULT NULL,
  `REGION_CONFIGURATION_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  FOREIGN KEY (`REGION_CONFIGURATION_ID`) REFERENCES `socialmedia_region` (`ID`),
  FOREIGN KEY (`PROVIDER_ID`) REFERENCES `socialmedia_provider` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

INSERT INTO `ais`.`socialmedia_telegram_config` (`ID`, `API_TOKEN`, `CHAT_ID`, `LANGUAGE_CODE`, `PROVIDER_ID`, `REGION_CONFIGURATION_ID`) VALUES ('1', '', '', 'de', '4', '2');
INSERT INTO `ais`.`socialmedia_telegram_config` (`ID`, `API_TOKEN`, `CHAT_ID`, `LANGUAGE_CODE`, `PROVIDER_ID`, `REGION_CONFIGURATION_ID`) VALUES ('2', '', '', 'it', '4', '2');
INSERT INTO `ais`.`socialmedia_telegram_config` (`ID`, `API_TOKEN`, `CHAT_ID`, `LANGUAGE_CODE`, `PROVIDER_ID`, `REGION_CONFIGURATION_ID`) VALUES ('3', '', '', 'en', '4', '2');
INSERT INTO `ais`.`socialmedia_telegram_config` (`ID`, `API_TOKEN`, `CHAT_ID`, `LANGUAGE_CODE`, `PROVIDER_ID`, `REGION_CONFIGURATION_ID`) VALUES ('4', '', '', 'de', '4', '3');
INSERT INTO `ais`.`socialmedia_telegram_config` (`ID`, `API_TOKEN`, `CHAT_ID`, `LANGUAGE_CODE`, `PROVIDER_ID`, `REGION_CONFIGURATION_ID`) VALUES ('5', '', '', 'it', '4', '3');
INSERT INTO `ais`.`socialmedia_telegram_config` (`ID`, `API_TOKEN`, `CHAT_ID`, `LANGUAGE_CODE`, `PROVIDER_ID`, `REGION_CONFIGURATION_ID`) VALUES ('6', '', '', 'en', '4', '3');
INSERT INTO `ais`.`socialmedia_telegram_config` (`ID`, `API_TOKEN`, `CHAT_ID`, `LANGUAGE_CODE`, `PROVIDER_ID`, `REGION_CONFIGURATION_ID`) VALUES ('7', '', '', 'de', '4', '1');
INSERT INTO `ais`.`socialmedia_telegram_config` (`ID`, `API_TOKEN`, `CHAT_ID`, `LANGUAGE_CODE`, `PROVIDER_ID`, `REGION_CONFIGURATION_ID`) VALUES ('8', '', '', 'it', '4', '1');
INSERT INTO `ais`.`socialmedia_telegram_config` (`ID`, `API_TOKEN`, `CHAT_ID`, `LANGUAGE_CODE`, `PROVIDER_ID`, `REGION_CONFIGURATION_ID`) VALUES ('9', '', '', 'en', '4', '1');
