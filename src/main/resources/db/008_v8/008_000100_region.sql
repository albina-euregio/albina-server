-- liquibase formatted sql
-- changeset albina:008_000100 failOnError:true

ALTER TABLE regions
DROP FOREIGN KEY IF EXISTS FKe74kalgqbt19lryn1tehx99r1;

ALTER TABLE regions
DROP COLUMN IF EXISTS SERVER_INSTANCE_ID;

ALTER TABLE regions
ADD COLUMN IF NOT EXISTS SERVER_IMAGES_URL varchar(191) COMMENT 'URL via which the static bulletin images can be retrieved. Used in the HTML bulletin and in e-mails.';

UPDATE regions
SET SERVER_IMAGES_URL = CONCAT(STATIC_URL, 'images/')
WHERE SERVER_IMAGES_URL IS NULL;
