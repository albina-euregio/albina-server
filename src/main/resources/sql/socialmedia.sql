create table socialmedia_channel (ID bigint not null auto_increment, NAME varchar(255), PROVIDER_ID bigint, primary key (ID)) engine=InnoDB;
create table socialmedia_channel_region (REGION_ID bigint not null, CHANNEL_ID bigint not null, primary key (REGION_ID, CHANNEL_ID)) engine=InnoDB;
create table socialmedia_provider (ID bigint not null auto_increment, NAME varchar(255), primary key (ID)) engine=InnoDB;
create table socialmedia_rapid_mail_config (ID bigint not null auto_increment, PASSWORD varchar(255), USERNAME varchar(255), PROVIDER_ID bigint, REGION_CONFIGURATION_ID bigint, primary key (ID)) engine=InnoDB;
create table socialmedia_region (ID bigint not null auto_increment, REGION_ID varchar(255), primary key (ID)) engine=InnoDB;
alter table socialmedia_rapid_mail_config add constraint UK_9nrw6e7krov3rpbscrsgcb2f4 unique (REGION_CONFIGURATION_ID);
alter table socialmedia_channel add constraint FKk91qceqhcsb647cvfqemq761u foreign key (PROVIDER_ID) references socialmedia_provider (ID);
alter table socialmedia_channel_region add constraint FKloji4gp4hhwl44gu7gskff1we foreign key (CHANNEL_ID) references socialmedia_channel (ID);
alter table socialmedia_channel_region add constraint FK568w5848nw13cwulpgywgcg7r foreign key (REGION_ID) references socialmedia_region (ID);
alter table socialmedia_rapid_mail_config add constraint FKnn0je2nbyeuqg768wtitomuks foreign key (PROVIDER_ID) references socialmedia_provider (ID);
alter table socialmedia_rapid_mail_config add constraint FK85jh34k3wrhfk7fivg9huu46s foreign key (REGION_CONFIGURATION_ID) references socialmedia_region (ID);
alter table socialmedia_region add constraint FKo8thusqux8xfm0hf0ku7akr1 foreign key (REGION_ID) references regions (ID);

INSERT INTO ais.socialmedia_provider (ID,NAME) VALUES(1,'Rapid Mail');
INSERT INTO ais.socialmedia_channel (ID,NAME,PROVIDER_ID) VALUES(1,'Email',1);

INSERT INTO ais.socialmedia_region (ID,REGION_ID) VALUES(1,'IT-32-TN');
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(1, 1);
INSERT INTO ais.socialmedia_rapid_mail_config (ID, PASSWORD, USERNAME, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(1, '093e1d3d97fd98ff6eb7879f3d4f7594b6bb6307', 'edc268e5e1a9abb27400ff967f40bdf2e95967d1', 1,1);

INSERT INTO ais.socialmedia_region (ID,REGION_ID) VALUES(2,'AT-07');
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(1, 2);
INSERT INTO ais.socialmedia_rapid_mail_config (ID, PASSWORD, USERNAME, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(2, '093e1d3d97fd98ff6eb7879f3d4f7594b6bb6307', 'edc268e5e1a9abb27400ff967f40bdf2e95967d1', 1,2);

INSERT INTO ais.socialmedia_region (ID,REGION_ID) VALUES(3,'IT-32-BZ');
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(1, 3);
INSERT INTO ais.socialmedia_rapid_mail_config (ID, PASSWORD, USERNAME, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(3, '093e1d3d97fd98ff6eb7879f3d4f7594b6bb6307', 'edc268e5e1a9abb27400ff967f40bdf2e95967d1', 1,3);
