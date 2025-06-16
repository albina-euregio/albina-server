-- liquibase formatted sql
-- changeset albina:000_000060 failOnError:true
-- comment create table user_role
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT EXISTS( SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'user_role' AND TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = SCHEMA())
CREATE TABLE user_role (
    USER_EMAIL varchar(191) NOT NULL,
    USER_ROLE varchar(191),
    CONSTRAINT FK_USER_ROLE_USER FOREIGN KEY (USER_EMAIL) REFERENCES users (EMAIL)
);

