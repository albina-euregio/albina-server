-- liquibase formatted sql
-- changeset albina:006_000040 failOnError:true

ALTER TABLE rapid_mail_configurations ADD LANGUAGE_CODE varchar(191) NULL;
ALTER TABLE rapid_mail_configurations ADD MAILINGLIST_NAME varchar(191) NULL;
ALTER TABLE rapid_mail_configurations ADD SUBJECT_MATTER varchar(191) NULL COMMENT 'bulletin/blog/media/media+';
