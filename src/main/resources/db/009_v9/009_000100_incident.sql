-- liquibase formatted sql
-- changeset albina:009_000100 failOnError:true

CREATE TABLE incidents (
    id            varchar(36)   NOT NULL,
    region_id     varchar(191)  NOT NULL,
    created_at    datetime(6)   NOT NULL,
    updated_at    datetime(6)   NOT NULL,
    report_status varchar(50)   GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(data, '$.reportStatus'))) STORED,
    date_time     varchar(30)   GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(data, '$.dateTime'))) STORED,
    data          json          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_incidents_region FOREIGN KEY (region_id) REFERENCES regions (ID),
    INDEX idx_incidents_region_status (region_id, report_status),
    INDEX idx_incidents_date_time (date_time)
);