CREATE TABLE `push_subscriptions` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `SUBSCRIBE_DATE` datetime NOT NULL,
  `AUTH` varchar(255) NOT NULL,
  `P256DH` varchar(255) NOT NULL,
  `ENDPOINT` varchar(1023) NOT NULL,
  `LANGUAGE_CODE` varchar(255) DEFAULT NULL,
  `REGION_ID` varchar(255) DEFAULT NULL,
  `FAILED_COUNT` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
)
