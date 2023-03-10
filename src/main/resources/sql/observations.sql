CREATE TABLE `observations` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `EVENT_TYPE` int(11) NOT NULL,
  `EVENT_DATE` datetime NOT NULL,
  `REPORT_DATE` datetime DEFAULT NULL,
  `AUTHOR_NAME` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `LOCATION_NAME` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `LATITUDE` double DEFAULT NULL,
  `LONGITUDE` double DEFAULT NULL,
  `ELEVATION` double DEFAULT NULL,
  `ASPECT` int(11) DEFAULT NULL,
  `REGION_ID` varchar(255) DEFAULT NULL,
  `CONTENT` longtext,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
