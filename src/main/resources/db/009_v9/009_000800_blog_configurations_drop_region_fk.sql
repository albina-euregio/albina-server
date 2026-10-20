-- liquibase formatted sql

-- changeset albina:009_000800 failOnError:true
-- comment drop foreign key blog_configurations.REGION_ID to allow configurations for regions not hosted by this instance
ALTER TABLE blog_configurations DROP FOREIGN KEY FK_GOOGLE_BLOGGER_CONFIGURATIONS_REGIONS;
