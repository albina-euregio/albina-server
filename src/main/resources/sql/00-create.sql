create table avalanche_bulletin_additional_users (AVALANCHE_BULLETIN_ID varchar(255) not null, ADDITIONAL_USER_NAME varchar(255)) engine=InnoDB
create table avalanche_bulletin_daytime_descriptions (ID varchar(255) not null, COMPLEXITY varchar(255), DANGER_RATING_ABOVE varchar(255), DANGER_RATING_BELOW varchar(255), ELEVATION integer, HAS_ELEVATION_DEPENDENCY bit, TERRAIN_FEATURE_ABOVE_TEXTCAT longtext, TERRAIN_FEATURE_BELOW_TEXTCAT longtext, TREELINE bit, AVALANCHE_PROBLEM_1_ID varchar(255), AVALANCHE_PROBLEM_2_ID varchar(255), AVALANCHE_PROBLEM_3_ID varchar(255), AVALANCHE_PROBLEM_4_ID varchar(255), AVALANCHE_PROBLEM_5_ID varchar(255), primary key (ID)) engine=InnoDB
create table avalanche_bulletin_published_regions (AVALANCHE_BULLETIN_ID varchar(255) not null, REGION_ID varchar(255)) engine=InnoDB
create table avalanche_bulletin_saved_regions (AVALANCHE_BULLETIN_ID varchar(255) not null, REGION_ID varchar(255)) engine=InnoDB
create table avalanche_bulletin_suggested_regions (AVALANCHE_BULLETIN_ID varchar(255) not null, REGION_ID varchar(255)) engine=InnoDB
create table avalanche_bulletin_texts (AVALANCHE_BULLETIN_ID varchar(255) not null, TEXTS_ID varchar(255) not null, TEXT_TYPE varchar(255) not null, primary key (AVALANCHE_BULLETIN_ID, TEXT_TYPE)) engine=InnoDB
create table avalanche_bulletins (ID varchar(255) not null, AV_ACTIVITY_COMMENT_NOTES longtext, AV_ACTIVITY_COMMENT_TEXTCAT longtext, AV_ACTIVITY_HIGHLIGHTS_NOTES longtext, AV_ACTIVITY_HIGHLIGHTS_TEXTCAT longtext, DANGER_PATTERN_1 varchar(255), DANGER_PATTERN_2 varchar(255), HAS_DAYTIME_DEPENDENCY bit, HIGHLIGHTS_TEXTCAT longtext, OWNER_REGION varchar(255), PUBLICATION_DATE datetime(6), SNOWPACK_STRUCTURE_COMMENT_NOTES longtext, SNOWPACK_STRUCTURE_COMMENT_TEXTCAT longtext, SNOWPACK_STRUCTURE_HIGHLIGHTS_NOTES longtext, SNOWPACK_STRUCTURE_HIGHLIGHTS_TEXTCAT longtext, TENDENCY varchar(255), TENDENCY_COMMENT_NOTES longtext, TENDENCY_COMMENT_TEXTCAT longtext, VALID_FROM datetime(6), VALID_UNTIL datetime(6), AFTERNOON_ID varchar(255), FORENOON_ID varchar(255), USER_ID varchar(255), primary key (ID)) engine=InnoDB
create table avalanche_problem_aspects (AVALANCHE_PROBLEM_ID varchar(255) not null, ASPECT integer) engine=InnoDB
create table avalanche_problems (ID varchar(255) not null, AVALANCHE_PROBLEM varchar(255), DANGER_RATING_DIRECTION varchar(255), AVALANCHE_SIZE varchar(255), AVALANCHE_SIZE_VALUE integer, DANGER_RATING varchar(255), DANGER_RATING_MODIFICATOR varchar(255), FREQUENCY varchar(255), FREQUENCY_VALUE integer, SNOWPACK_STABILITY varchar(255), SNOWPACK_STABILITY_VALUE integer, ELEVATION_HIGH integer, ELEVATION_LOW integer, ARTIFICIAL_AVALANCHE_RELEASE_PROBABILITY varchar(255), ARTIFICIAL_AVALANCHE_SIZE varchar(255), ARTIFICIAL_DANGER_RATING varchar(255), ARTIFICIAL_HAZARD_SITE_DISTRIBUTION varchar(255), NATURAL_AVALANCHE_RELEASE_PROBABILITY varchar(255), NATURAL_DANGER_RATING varchar(255), NATURAL_HAZARD_SITE_DISTRIBUTION varchar(255), TERRAIN_FEATURE_TEXTCAT longtext, TREELINE_HIGH bit, TREELINE_LOW bit, primary key (ID)) engine=InnoDB
create table avalanche_reports (ID varchar(255) not null, CAAML_V5_CREATED bit, CAAML_V6_CREATED bit, DATE datetime(6), EMAIL_CREATED bit, HTML_CREATED bit, JSON_CREATED bit, JSON_STRING longtext, MAP_CREATED bit, MEDIA_FILE_UPLOADED bit, PDF_CREATED bit, PUSH_SENT bit, STATUS integer, TELEGRAM_SENT bit, TIMESTAMP datetime(6), REGION_ID varchar(255), USER_ID varchar(255), primary key (ID)) engine=InnoDB
create table chat_messages (ID varchar(255) not null, CHAT_ID integer, DATETIME datetime(6), TEXT varchar(255), USERNAME varchar(255), primary key (ID)) engine=InnoDB
create table google_blogger_configurations (ID bigint not null auto_increment, API_KEY varchar(255), BLOG_API_URL varchar(255), BLOG_ID varchar(255), BLOG_URL varchar(255), LANGUAGE_CODE varchar(255), REGION_ID varchar(255), primary key (ID)) engine=InnoDB
create table observations (ID bigint not null auto_increment, ASPECT varchar(255), AUTHOR_NAME varchar(255), CONTENT longtext, ELEVATION double precision, EVENT_DATE datetime(6), EVENT_TYPE integer, LATITUDE double precision, LOCATION_NAME varchar(255), LONGITUDE double precision, REGION_ID varchar(255), REPORT_DATE datetime(6), primary key (ID)) engine=InnoDB
create table push_configurations (ID bigint not null auto_increment, VAPID_PRIVATE_KEY varchar(255), VAPID_PUBLIC_KEY varchar(255), primary key (ID)) engine=InnoDB
create table push_subscriptions (ID bigint not null auto_increment, AUTH varchar(255), ENDPOINT varchar(1023), FAILED_COUNT integer, LANGUAGE_CODE varchar(255), P256DH varchar(255), REGION_ID varchar(255), SUBSCRIBE_DATE datetime(6), primary key (ID)) engine=InnoDB
create table rapid_mail_configurations (ID bigint not null auto_increment, PASSWORD varchar(255), USERNAME varchar(255), REGION_ID varchar(255), primary key (ID)) engine=InnoDB
create table region_hierarchy (SUB_REGION_ID varchar(255) not null, SUPER_REGION_ID varchar(255) not null, primary key (SUPER_REGION_ID, SUB_REGION_ID)) engine=InnoDB
create table region_neighbors (REGION_ID varchar(255) not null, NEIGHBOR_REGION_ID varchar(255) not null, primary key (REGION_ID, NEIGHBOR_REGION_ID)) engine=InnoDB
create table regions (ID varchar(255) not null, CREATE_CAAML_V5 bit, CREATE_CAAML_V6 bit, CREATE_JSON bit, CREATE_MAPS bit, CREATE_PDF bit, CREATE_SIMPLE_HTML bit, EMAIL_COLOR varchar(255), ENABLE_AVALANCHE_PROBLEM_CORNICES bit, ENABLE_AVALANCHE_PROBLEM_NO_DISTINCT_AVALANCHE_PROBLEM bit, ENABLE_MEDIA_FILE bit, GEO_DATA_DIRECTORY varchar(255), IMAGE_COLORBAR_BW_PATH varchar(255), IMAGE_COLORBAR_COLOR_PATH varchar(255), MAP_CENTER_LAT double precision, MAP_CENTER_LNG double precision, MAP_LOGO_BW_PATH varchar(255), MAP_LOGO_COLOR_PATH varchar(255), MAP_LOGO_POSITION varchar(255), MAP_X_MAX integer, MAP_X_MIN integer, MAP_Y_MAX integer, MAP_Y_MIN integer, MICRO_REGIONS integer, PDF_COLOR varchar(255), PDF_FOOTER_LOGO bit, PDF_FOOTER_LOGO_BW_PATH varchar(255), PDF_FOOTER_LOGO_COLOR_PATH varchar(255), PDF_MAP_HEIGHT integer, PDF_MAP_WIDTH_AM_PM integer, PDF_MAP_WIDTH_FD integer, PDF_MAP_Y_AM_PM integer, PDF_MAP_Y_FD integer, PUBLISH_BLOGS bit, PUBLISH_BULLETINS bit, SEND_EMAILS bit, SEND_PUSH_NOTIFICATIONS bit, SEND_TELEGRAM_MESSAGES bit, SHOW_MATRIX bit, SIMPLE_HTML_TEMPLATE_NAME varchar(255), SERVER_INSTANCE_ID bigint, primary key (ID)) engine=InnoDB
create table server_instances (ID bigint not null auto_increment, API_URL varchar(255), EXTERNAL_SERVER bit, HTML_DIRECTORY varchar(255), MAP_PRODUCTION_URL varchar(255), MAPS_PATH varchar(255), MEDIA_PATH varchar(255), NAME varchar(255), PASSWORD varchar(255), PDF_DIRECTORY varchar(255), PUBLISH_AT_5PM bit, PUBLISH_AT_8PM bit, SERVER_IMAGES_URL varchar(255), USER_NAME varchar(255), primary key (ID)) engine=InnoDB
create table subscriber_regions (SUBSCRIBER_ID varchar(255) not null, REGION_ID varchar(255) not null) engine=InnoDB
create table subscribers (EMAIL varchar(255) not null, CONFIRMED bit, LANGUAGE varchar(255), PDF_ATTACHMENT bit, primary key (EMAIL)) engine=InnoDB
create table telegram_configurations (ID bigint not null auto_increment, API_TOKEN varchar(255), CHAT_ID varchar(255), LANGUAGE_CODE varchar(255), REGION_ID varchar(255), primary key (ID)) engine=InnoDB
create table text_parts (TEXTS_ID varchar(255) not null, LANGUAGE_CODE varchar(255), TEXT longtext) engine=InnoDB
create table texts (ID varchar(255) not null, primary key (ID)) engine=InnoDB
create table user_region (USER_EMAIL varchar(255) not null, REGION_ID varchar(255) not null, primary key (USER_EMAIL, REGION_ID)) engine=InnoDB
create table user_region_role_links (ID bigint not null auto_increment, ROLE varchar(255), REGION_ID varchar(255), USER_EMAIL varchar(255), primary key (ID)) engine=InnoDB
create table user_role (USER_EMAIL varchar(255) not null, USER_ROLE varchar(255)) engine=InnoDB
create table users (EMAIL varchar(255) not null, IMAGE LONGBLOB, NAME varchar(255), ORGANIZATION varchar(255), PASSWORD varchar(255), primary key (EMAIL)) engine=InnoDB
alter table avalanche_bulletin_texts add constraint UK_ptswdete5bai6keem5mqpy1qu unique (TEXTS_ID)
create index avalanche_reports_DATE_IDX on avalanche_reports (DATE)
alter table avalanche_bulletin_additional_users add constraint FKb66t7jbpnalfa3f7dem9m7i3x foreign key (AVALANCHE_BULLETIN_ID) references avalanche_bulletins (ID)
alter table avalanche_bulletin_daytime_descriptions add constraint FKqgs5r0o3h400uattltrf5ro9p foreign key (AVALANCHE_PROBLEM_1_ID) references avalanche_problems (ID)
alter table avalanche_bulletin_daytime_descriptions add constraint FK8o1sce1w8f39dbd7o8905a6mh foreign key (AVALANCHE_PROBLEM_2_ID) references avalanche_problems (ID)
alter table avalanche_bulletin_daytime_descriptions add constraint FK29v1h70w7smuv5uksp329xk1p foreign key (AVALANCHE_PROBLEM_3_ID) references avalanche_problems (ID)
alter table avalanche_bulletin_daytime_descriptions add constraint FK2idj377s48ea0gfm7selv98i1 foreign key (AVALANCHE_PROBLEM_4_ID) references avalanche_problems (ID)
alter table avalanche_bulletin_daytime_descriptions add constraint FKhrc3r32lf8fu668perfr6e8lw foreign key (AVALANCHE_PROBLEM_5_ID) references avalanche_problems (ID)
alter table avalanche_bulletin_published_regions add constraint FKq791759t4ngwy3xnjcsarubd9 foreign key (AVALANCHE_BULLETIN_ID) references avalanche_bulletins (ID)
alter table avalanche_bulletin_saved_regions add constraint FK51xgpiadqoop7mkuw02n2kvhl foreign key (AVALANCHE_BULLETIN_ID) references avalanche_bulletins (ID)
alter table avalanche_bulletin_suggested_regions add constraint FKnvuoxj1thuu09jriacj91e3c9 foreign key (AVALANCHE_BULLETIN_ID) references avalanche_bulletins (ID)
alter table avalanche_bulletin_texts add constraint FKrrg7cyd64c8i2brbfnai6jc7v foreign key (TEXTS_ID) references texts (ID)
alter table avalanche_bulletin_texts add constraint FKfr6qmtqtki9hj78bm72gtt8bp foreign key (AVALANCHE_BULLETIN_ID) references avalanche_bulletins (ID)
alter table avalanche_bulletins add constraint FKs8wvyul8jsdqfnp0epdgvk3c2 foreign key (AFTERNOON_ID) references avalanche_bulletin_daytime_descriptions (ID)
alter table avalanche_bulletins add constraint FKb5w9xw8i333rismcrri62umqw foreign key (FORENOON_ID) references avalanche_bulletin_daytime_descriptions (ID)
alter table avalanche_bulletins add constraint FKpklhevlq5xr3l8mq0cll6aoem foreign key (USER_ID) references users (EMAIL)
alter table avalanche_problem_aspects add constraint FKd1xfm95tvk97y0ab8l2eu125x foreign key (AVALANCHE_PROBLEM_ID) references avalanche_problems (ID)
alter table avalanche_reports add constraint FKj2jj2lgknd44i41wsagrprpro foreign key (REGION_ID) references regions (ID)
alter table avalanche_reports add constraint FKajgejvjpourfacpfjn6ndlb6c foreign key (USER_ID) references users (EMAIL)
alter table google_blogger_configurations add constraint FKe8ekul2ewkhmwkmwa5j3q8o9d foreign key (REGION_ID) references regions (ID)
alter table rapid_mail_configurations add constraint FK43imjvb3ly5k5m1tc8nsllf47 foreign key (REGION_ID) references regions (ID)
alter table region_hierarchy add constraint FKb39ivcay57i43jenoty5oe9kv foreign key (SUPER_REGION_ID) references regions (ID)
alter table region_hierarchy add constraint FKjedd3newdligaejyttbvggv3y foreign key (SUB_REGION_ID) references regions (ID)
alter table region_neighbors add constraint FK5yviwfqh8ubmtw7ypnuxp6u6t foreign key (NEIGHBOR_REGION_ID) references regions (ID)
alter table region_neighbors add constraint FKsp2919qu9ds8xsn731ye2xmit foreign key (REGION_ID) references regions (ID)
alter table regions add constraint FKe74kalgqbt19lryn1tehx99r1 foreign key (SERVER_INSTANCE_ID) references server_instances (ID)
alter table subscriber_regions add constraint FKqecsb6dfglu61bfbwnn05vj6p foreign key (REGION_ID) references regions (ID)
alter table subscriber_regions add constraint FKcedrtfonb8j38kg7l4omkli7y foreign key (SUBSCRIBER_ID) references subscribers (EMAIL)
alter table telegram_configurations add constraint FKlkl3bidyff9q9boyjgua3evn0 foreign key (REGION_ID) references regions (ID)
alter table text_parts add constraint FKfynmdlr1up76i0oe06jixe48t foreign key (TEXTS_ID) references texts (ID)
alter table user_region add constraint FKbeiyvshltxggmnnjbu87shdgc foreign key (REGION_ID) references regions (ID)
alter table user_region add constraint FKhu6mvkdomq5v4pneu81p7mirn foreign key (USER_EMAIL) references users (EMAIL)
alter table user_region_role_links add constraint FKmkucw3bgvaurlvqbqj3w9sncs foreign key (REGION_ID) references regions (ID)
alter table user_region_role_links add constraint FKef1pg0uirvxqdq5bgq33kd2cp foreign key (USER_EMAIL) references users (EMAIL)
alter table user_role add constraint FK4qllg1csw4u7ir0ysgy0eykqg foreign key (USER_EMAIL) references users (EMAIL)
