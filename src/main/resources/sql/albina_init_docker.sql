INSERT INTO server_instances (API_URL, EXTERNAL_SERVER, HTML_DIRECTORY, MAP_PRODUCTION_URL, MAPS_PATH, MEDIA_PATH, NAME, PASSWORD, PDF_DIRECTORY, PUBLISH_AT_5PM, PUBLISH_AT_8PM, SERVER_IMAGES_URL, USER_NAME)
VALUES ('https://api.avalanche.report/albina/api/', 0, '/var/www/static.avalanche.report/simple', '/opt/avalanche-warning-maps', '/var/www/static.avalanche.report/bulletins', '/var/www/static.avalanche.report/media_files', 'Avalanche.report', '', '/var/www/static.avalanche.report/bulletins', 1, 1, 'https://admin.avalanche.report/images/', 'info@avalanche.report');


INSERT INTO regions (ID, CREATE_CAAML_V5, CREATE_CAAML_V6, CREATE_JSON, CREATE_MAPS, CREATE_PDF, CREATE_SIMPLE_HTML, EMAIL_COLOR, ENABLE_MEDIA_FILE, GEO_DATA_DIRECTORY, IMAGE_COLORBAR_BW_PATH, IMAGE_COLORBAR_COLOR_PATH, MAP_CENTER_LAT, MAP_CENTER_LNG, MAP_LOGO_BW_PATH, MAP_LOGO_COLOR_PATH, MAP_LOGO_POSITION, MAP_X_MAX, MAP_X_MIN, MAP_Y_MAX, MAP_Y_MIN, MICRO_REGIONS, PDF_COLOR, PDF_FOOTER_LOGO, PDF_FOOTER_LOGO_BW_PATH, PDF_FOOTER_LOGO_COLOR_PATH, PDF_MAP_HEIGHT, PDF_MAP_WIDTH_AM_PM, PDF_MAP_WIDTH_FD, PDF_MAP_Y_AM_PM, PDF_MAP_Y_FD, PUBLISH_BLOGS, PUBLISH_BULLETINS, SEND_EMAILS, SEND_PUSH_NOTIFICATIONS, SEND_TELEGRAM_MESSAGES, SIMPLE_HTML_TEMPLATE_NAME, SERVER_INSTANCE_ID, SHOW_MATRIX)
VALUES ('EUREGIO', 1, 1, 1, 1, 1, 1, '1AABFF', 0, 'geodata.Euregio/', 'logo/grey/colorbar.gif', 'logo/color/colorbar.gif', 0.0, 0.0, 'images/logo/grey/euregio.png', 'images/logo/color/euregio.png', 'bottomright', 1464000, 1104000, 6047000, 5687000, 0, '00ACFB', 1, 'logo/grey/euregio.png', 'logo/color/euregio.png', 270, 270, 420, 130, 250, 0, 0, 0, 0, 0, 'simple-bulletin.min.html', 1, 1),
       ('AT-07', 1, 1, 1, 1, 1, 1, '1AABFF', 1, 'geodata.Euregio/AT-07/', 'logo/grey/colorbar.gif', 'logo/color/colorbar.gif', 47.1, 11.44, '', '', 'bottomright', 1452000, 1116000, 6053000, 5829000, 36, '00ACFB', 1, 'logo/grey/euregio.png', 'logo/color/euregio.png', 267, 400, 500, 130, 290, 1, 1, 1, 1, 1, 'simple-bulletin.min.html', 1, 0),
       ('IT-32-BZ', 1, 1, 1, 1, 1, 1, '1AABFF', 0, 'geodata.Euregio/IT-32-BZ/', 'logo/grey/colorbar.gif', 'logo/color/colorbar.gif', 46.65, 11.4, '', '', 'bottomright', 1400000, 1145000, 5939000, 5769000, 31, '00ACFB', 1, 'logo/grey/euregio.png', 'logo/color/euregio.png', 267, 400, 500, 130, 290, 1, 1, 1, 1, 1, 'simple-bulletin.min.html', 1, 0),
       ('IT-32-TN', 1, 1, 1, 1, 1, 1, '1AABFF', 0, 'geodata.Euregio/IT-32-TN/', 'logo/grey/colorbar.gif', 'logo/color/colorbar.gif', 46.05, 11.07, '', '', 'bottomright', 1358000, 1133000, 5842000, 5692000, 21, '00ACFB', 1, 'logo/grey/euregio.png', 'logo/color/euregio.png', 267, 400, 500, 130, 290, 1, 1, 1, 1, 1, 'simple-bulletin.min.html', 1, 0);


# zee3tohDuu7Zi
INSERT INTO users (EMAIL, NAME, PASSWORD)
VALUES ('info@avalanche.report', 'Avalanche.report', '$2a$10$y8hK8NYD4MAErYd.1ZWLDO8.Eu3MFBD.98LFyIHvRf95Tgx3rQuBa');


INSERT INTO user_region (USER_EMAIL,REGION_ID) VALUES
	 ('info@avalanche.report','AT-07'),
	 ('info@avalanche.report','IT-32-BZ'),
	 ('info@avalanche.report','IT-32-TN');


INSERT INTO user_role (USER_EMAIL, USER_ROLE)
VALUES ('info@avalanche.report', 'ADMIN'),
       ('info@avalanche.report', 'FORECASTER');
