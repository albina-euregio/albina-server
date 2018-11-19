-- drop table avalanche_reports;
-- create table avalanche_reports (ID varchar(255) not null, CAAML_CREATED bit, DATE datetime, EMAIL_CREATED bit, MAP_CREATED bit, PDF_CREATED bit, REGION varchar(255), REVISION integer, STATIC_WIDGET_CREATED bit, STATUS integer, TELEGRAM_SENT bit, TIMESTAMP datetime, WHATSAPP_SENT bit, USER_ID varchar(255), primary key (ID)) engine=InnoDB;

-- insert into users (EMAIL, PASSWORD, NAME, ORGANIZATION, IMAGE) values ('miorandi@clesius.it', '$2a$10$dlBsNKPsLEMgOZIxVlpw0uqmz.E/aPZTEn04tslFNtsgGuyyjXPOi', 'Denis Miorandi', 'xxxx', '');
-- insert into user_role (USER_EMAIL, USER_ROLE) values ('miorandi@clesius.it', 'ADMIN');
-- insert into user_region (USER_EMAIL, USER_REGION) values ('miorandi@clesius.it', 'AT-07');
-- insert into user_region (USER_EMAIL, USER_REGION) values ('miorandi@clesius.it', 'IT-32-TN');
-- insert into user_region (USER_EMAIL, USER_REGION) values ('miorandi@clesius.it', 'IT-32-BZ');
-- insert into user_region (USER_EMAIL, USER_REGION) values ('miorandi@clesius.it', 'AT-06');

create table socialmedia_channel (ID bigint not null auto_increment, NAME varchar(255), PROVIDER_ID bigint, primary key (ID)) engine=InnoDB;
create table socialmedia_channel_region (REGION_ID bigint not null, CHANNEL_ID bigint not null, primary key (REGION_ID, CHANNEL_ID)) engine=InnoDB;
create table socialmedia_messenger_people_config (ID bigint not null auto_increment, API_KEY varchar(255), CHANNEL_NAME varchar(255), MOBILE_NUMBER varchar(255), PROVIDER_ID bigint, REGION_CONFIGURATION_ID bigint, primary key (ID)) engine=InnoDB;
create table socialmedia_provider (ID bigint not null auto_increment, NAME varchar(255), primary key (ID)) engine=InnoDB;
create table socialmedia_rapid_mail_config (ID bigint not null auto_increment, PASSWORD varchar(255), USERNAME varchar(255), PROVIDER_ID bigint, REGION_CONFIGURATION_ID bigint, primary key (ID)) engine=InnoDB;
create table socialmedia_rapid_mail_recipients (ID bigint not null auto_increment, REFERENCE varchar(255), RAPIDMAIL_CONFIG_ID bigint, primary key (ID)) engine=InnoDB;
create table socialmedia_region (ID bigint not null auto_increment, REGION_ID varchar(255), primary key (ID)) engine=InnoDB;
create table socialmedia_shipment (id bigint not null auto_increment, REQUEST longtext, RESPONSE longtext, DATE datetime, ID_MP varchar(255), ID_RM varchar(255), ID_TW varchar(255), LANGUAGE varchar(255), NAME varchar(255), PROVIDER_ID bigint, REGION_ID bigint, primary key (id)) engine=InnoDB;
create table socialmedia_twitter_config (ID bigint not null auto_increment, ACCESS_KEY varchar(255), ACCESS_SECRET varchar(255), CONSUMER_KEY varchar(255), CONSUMER_SECRET varchar(255), PROVIDER_ID bigint, REGION_CONFIGURATION_ID bigint, primary key (ID)) engine=InnoDB;
alter table socialmedia_messenger_people_config add constraint UK_kc7ihv39ydkx77bsavmijx3wa unique (REGION_CONFIGURATION_ID);
alter table socialmedia_rapid_mail_config add constraint UK_9nrw6e7krov3rpbscrsgcb2f4 unique (REGION_CONFIGURATION_ID);
alter table socialmedia_twitter_config add constraint UK_pw1q105bcnnu6sqbwneiyy1dx unique (REGION_CONFIGURATION_ID);
alter table socialmedia_channel add constraint FKk91qceqhcsb647cvfqemq761u foreign key (PROVIDER_ID) references socialmedia_provider (ID);
alter table socialmedia_channel_region add constraint FKloji4gp4hhwl44gu7gskff1we foreign key (CHANNEL_ID) references socialmedia_channel (ID);
alter table socialmedia_channel_region add constraint FK568w5848nw13cwulpgywgcg7r foreign key (REGION_ID) references socialmedia_region (ID);
alter table socialmedia_messenger_people_config add constraint FK5ppds3aamdocsqbagq872f2nd foreign key (PROVIDER_ID) references socialmedia_provider (ID);
alter table socialmedia_messenger_people_config add constraint FK44lca0rac5fhn86iglrfdsjsj foreign key (REGION_CONFIGURATION_ID) references socialmedia_region (ID);
alter table socialmedia_rapid_mail_config add constraint FKnn0je2nbyeuqg768wtitomuks foreign key (PROVIDER_ID) references socialmedia_provider (ID);
alter table socialmedia_rapid_mail_config add constraint FK85jh34k3wrhfk7fivg9huu46s foreign key (REGION_CONFIGURATION_ID) references socialmedia_region (ID);
alter table socialmedia_rapid_mail_recipients add constraint FKmldewm06xjeqt17vb4qvog1y foreign key (RAPIDMAIL_CONFIG_ID) references socialmedia_rapid_mail_config (ID);
alter table socialmedia_region add constraint FKo8thusqux8xfm0hf0ku7akr1 foreign key (REGION_ID) references regions (ID);
alter table socialmedia_shipment add constraint FK16idaq3mka4lx7k7lr2rg2fex foreign key (PROVIDER_ID) references socialmedia_provider (ID);
alter table socialmedia_shipment add constraint FKm7wkc3jjerlkk8qlh2atuj2cb foreign key (REGION_ID) references socialmedia_region (ID);
alter table socialmedia_twitter_config add constraint FK2xkciggpik363hck1arnmiek2 foreign key (PROVIDER_ID) references socialmedia_provider (ID);
alter table socialmedia_twitter_config add constraint FK67s3ylki2g7h3qpkn4i04636i foreign key (REGION_CONFIGURATION_ID) references socialmedia_region (ID);

INSERT INTO ais.socialmedia_provider (ID,NAME) VALUES(1,'Rapid Mail');
INSERT INTO ais.socialmedia_provider (ID,NAME) VALUES(2,'Messenger People');
INSERT INTO ais.socialmedia_provider (ID,NAME) VALUES(3,'Twitter');
INSERT INTO ais.socialmedia_channel (ID,NAME,PROVIDER_ID) VALUES(1,'Email',1);
INSERT INTO ais.socialmedia_channel (ID,NAME,PROVIDER_ID) VALUES(2,'Whatsapp',2);
INSERT INTO ais.socialmedia_channel (ID,NAME,PROVIDER_ID) VALUES(3,'Telegram',2);
INSERT INTO ais.socialmedia_channel (ID,NAME,PROVIDER_ID) VALUES(4,'Insta',2);
INSERT INTO ais.socialmedia_channel (ID,NAME,PROVIDER_ID) VALUES(5,'Twitter',3);

INSERT INTO ais.socialmedia_region (ID,REGION_ID) VALUES(1,'IT-32-TN');
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(1, 1);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(2, 1);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(3, 1);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(4, 1);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(5, 1);

--Avalanche.report
--j3z#9B%9​​
INSERT INTO ais.socialmedia_rapid_mail_config (ID, PASSWORD, USERNAME, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(1, 'edc268e5e1a9abb27400ff967f40bdf2e95967d1', '093e1d3d97fd98ff6eb7879f3d4f7594b6bb6307', 1,1);
INSERT INTO ais.socialmedia_rapid_mail_recipients (REFERENCE, RAPIDMAIL_CONFIG_ID) VALUES('r1_Recipient 1', 1);
INSERT INTO ais.socialmedia_rapid_mail_recipients (REFERENCE, RAPIDMAIL_CONFIG_ID) VALUES('r1_Recipient 2', 1);
INSERT INTO ais.socialmedia_messenger_people_config (ID, API_KEY, CHANNEL_NAME, MOBILE_NUMBER, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(1, 'a1e6d5387c979b039040447af4a4d20a_11513_9fc5a49fc674b5b2750ad90a7', 'r1_cn1', 'r1_mn1', 2,1);
INSERT INTO ais.socialmedia_twitter_config (ID, ACCESS_KEY, ACCESS_SECRET, CONSUMER_KEY, CONSUMER_SECRET, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(1, 'r1_ak1', 'r1_as1', 'r1_ck1', 'r1_cs1', 3,1);

INSERT INTO ais.socialmedia_region (ID,REGION_ID) VALUES(2,'AT-07');
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(1, 2);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(2, 2);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(3, 2);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(4, 2);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(5, 2);
INSERT INTO ais.socialmedia_rapid_mail_config (ID, PASSWORD, USERNAME, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(2, 'edc268e5e1a9abb27400ff967f40bdf2e95967d1', '093e1d3d97fd98ff6eb7879f3d4f7594b6bb6307', 1,2);
INSERT INTO ais.socialmedia_rapid_mail_recipients (REFERENCE, RAPIDMAIL_CONFIG_ID) VALUES('r2_Recipient 1', 2);
INSERT INTO ais.socialmedia_rapid_mail_recipients (REFERENCE, RAPIDMAIL_CONFIG_ID) VALUES('r2_Recipient 2', 2);
INSERT INTO ais.socialmedia_messenger_people_config (ID, API_KEY, CHANNEL_NAME, MOBILE_NUMBER, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(2, 'a1e6d5387c979b039040447af4a4d20a_11513_9fc5a49fc674b5b2750ad90a7', 'r2_cn', 'r2_mn', 2,2);
INSERT INTO ais.socialmedia_twitter_config (ID, ACCESS_KEY, ACCESS_SECRET, CONSUMER_KEY, CONSUMER_SECRET, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(2, 'r2_ak', 'r2_as', 'r2_ck', 'r2_cs1', 3,2);

INSERT INTO ais.socialmedia_region (ID,REGION_ID) VALUES(3,'IT-32-BZ');
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(1, 3);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(2, 3);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(3, 3);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(4, 3);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(5, 3);
INSERT INTO ais.socialmedia_rapid_mail_config (ID, PASSWORD, USERNAME, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(3, 'edc268e5e1a9abb27400ff967f40bdf2e95967d1', '093e1d3d97fd98ff6eb7879f3d4f7594b6bb6307', 1,3);
INSERT INTO ais.socialmedia_rapid_mail_recipients (REFERENCE, RAPIDMAIL_CONFIG_ID) VALUES('r3_Recipient 1', 3);
INSERT INTO ais.socialmedia_rapid_mail_recipients (REFERENCE, RAPIDMAIL_CONFIG_ID) VALUES('r3_Recipient 2', 3);
INSERT INTO ais.socialmedia_messenger_people_config (ID, API_KEY, CHANNEL_NAME, MOBILE_NUMBER, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(3, 'a1e6d5387c979b039040447af4a4d20a_11513_9fc5a49fc674b5b2750ad90a7', 'r3_cn', 'r3_mn', 2,3);
INSERT INTO ais.socialmedia_twitter_config (ID, ACCESS_KEY, ACCESS_SECRET, CONSUMER_KEY, CONSUMER_SECRET, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(3, 'r3_ak', 'r3_as', 'r3_ck', 'r3_cs1', 3,3);

INSERT INTO ais.socialmedia_region (ID,REGION_ID) VALUES(4,'AT-06');
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(1, 4);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(2, 4);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(3, 4);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(4, 4);
INSERT INTO ais.socialmedia_channel_region (CHANNEL_ID, REGION_ID) VALUES(5, 4);
INSERT INTO ais.socialmedia_rapid_mail_config (ID, PASSWORD, USERNAME, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(4, 'edc268e5e1a9abb27400ff967f40bdf2e95967d1', '093e1d3d97fd98ff6eb7879f3d4f7594b6bb6307', 1,4);
INSERT INTO ais.socialmedia_rapid_mail_recipients (REFERENCE, RAPIDMAIL_CONFIG_ID) VALUES('r4_Recipient 1', 4);
INSERT INTO ais.socialmedia_rapid_mail_recipients (REFERENCE, RAPIDMAIL_CONFIG_ID) VALUES('r4_Recipient 2', 4);
INSERT INTO ais.socialmedia_messenger_people_config (ID, API_KEY, CHANNEL_NAME, MOBILE_NUMBER, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(4, 'a1e6d5387c979b039040447af4a4d20a_11513_9fc5a49fc674b5b2750ad90a7', 'r4_cn', 'r4_mn', 2,4);
INSERT INTO ais.socialmedia_twitter_config (ID, ACCESS_KEY, ACCESS_SECRET, CONSUMER_KEY, CONSUMER_SECRET, PROVIDER_ID, REGION_CONFIGURATION_ID) VALUES(4, 'r4_ak', 'r4_as', 'r4_ck', 'r4_cs1', 3,4);


