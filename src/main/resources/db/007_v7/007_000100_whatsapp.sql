-- liquibase formatted sql
-- changeset christina:007_000100 failOnError:true

CREATE TABLE whatsapp_configurations (
    ID bigint NOT NULL AUTO_INCREMENT,
    API_TOKEN varchar(191),
    CHAT_ID varchar(191),
    LANGUAGE_CODE varchar(191),
    REGION_ID varchar(191),
    PRIMARY KEY (ID),
    CONSTRAINT FK_WHATSAPP_CONFIGURATIONS_REGIONS FOREIGN KEY (REGION_ID) REFERENCES regions (ID)
);

-- changeset christina:007_000101 failOnError:true
ALTER TABLE avalanche_reports ADD COLUMN WHATSAPP_SENT bit(1) DEFAULT 0;

-- changeset christina:007_000102 failOnError:true
ALTER TABLE regions ADD COLUMN SEND_WHATSAPP_MESSAGES bit(1) DEFAULT 0;
