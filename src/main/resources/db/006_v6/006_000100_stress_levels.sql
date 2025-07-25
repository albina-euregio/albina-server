-- liquibase formatted sql
-- changeset albina:006_000100 failOnError:true

CREATE TABLE user_stress_levels
(
    USER_ID      VARCHAR(255) NOT NULL,
    DATE         date         NOT NULL,
    STRESS_LEVEL INT          NULL,
    LAST_UPDATED datetime     NOT NULL,
    CONSTRAINT PK_STRESS_LEVELS PRIMARY KEY (USER_ID, DATE),
    CONSTRAINT FK_STRESS_LEVELS_ON_USER FOREIGN KEY (USER_ID) REFERENCES users (EMAIL)
);
