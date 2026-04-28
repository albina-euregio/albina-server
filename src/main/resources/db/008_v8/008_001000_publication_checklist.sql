-- liquibase formatted sql
-- changeset albina:008_001000 failOnError:true

CREATE TABLE publication_checklists (
	ID varchar(191) NOT NULL,
	DATE datetime NOT NULL,
	TIMESTAMP datetime NOT NULL,
	REGION_ID varchar(191) NOT NULL,
	USER_ID varchar(191) NOT NULL,
	PRIMARY KEY (ID),
	CONSTRAINT publication_checklists_region_fk FOREIGN KEY (REGION_ID) REFERENCES regions (ID),
	CONSTRAINT publication_checklists_user_fk FOREIGN KEY (USER_ID) REFERENCES users (EMAIL),
	INDEX publication_checklists_DATE_REGION_IDX (DATE, REGION_ID)
);

CREATE TABLE publication_checklist_items (
	ID varchar(191) NOT NULL,
	CHECKLIST_ID varchar(191) NOT NULL,
	PUBLICATION_CHANNEL varchar(191) NOT NULL,
	OK_VALUE bit DEFAULT NULL,
	PROBLEM_DESCRIPTION longtext DEFAULT NULL,
	PRIMARY KEY (ID),
	CONSTRAINT publication_checklist_items_checklist_fk FOREIGN KEY (CHECKLIST_ID) REFERENCES publication_checklists (ID),
	INDEX publication_checklist_items_CHECKLIST_IDX (CHECKLIST_ID)
);