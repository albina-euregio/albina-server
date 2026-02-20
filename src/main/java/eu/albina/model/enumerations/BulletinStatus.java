// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum BulletinStatus {
	republished, resubmitted, updated, published, submitted, draft, missing;

	private static final Logger logger = LoggerFactory.getLogger(BulletinStatus.class);

	public static boolean isDraftOrUpdated(BulletinStatus status) {
		return status == draft || status == updated;
	}

	public BulletinStatus publishReport() {
		return switch (this) {
			case missing -> {
				logger.warn("Bulletins have to be created first!");
				yield missing;
			}
			case draft -> {
				logger.warn("Bulletins have to be submitted first!");
				yield updated;
			}
			case submitted -> {
				logger.info("Status set to PUBLISHED");
				yield published;
			}
			case published -> {
				logger.warn("Bulletins already published!");
				yield published;
			}
			case updated -> {
				logger.warn("Bulletins have to be resubmitted first!");
				yield updated;
			}
			case resubmitted -> {
				logger.info("Status set to REPUBLISHED");
				yield republished;
			}
			case republished -> {
				logger.warn("Bulletins already republished!");
				yield republished;
			}
		};
	}

	public BulletinStatus submitReport() {
		return switch (this) {
			case missing -> {
				logger.warn("Bulletins have to be created first!");
				yield missing;
			}
			case draft -> {
				logger.info("Status set to SUBMITTED");
				yield submitted;
			}
			case submitted -> {
				logger.warn("Bulletins already submitted!");
				yield submitted;
			}
			case published -> {
				logger.warn("Bulletins already published!");
				yield published;
			}
			case updated -> {
				logger.info("Status set to RESUBMITTED");
				yield resubmitted;
			}
			case resubmitted -> {
				logger.info("Bulletins already resubmitted!");
				yield resubmitted;
			}
			case republished -> {
				logger.warn("Bulletins already republished!");
				yield republished;
			}
		};
	}

	public BulletinStatus saveReport() {
		return switch (this) {
			case missing, republished, resubmitted, updated, published -> updated;
			case draft, submitted -> draft;
		};
	}

	public int comparePublicationStatus(BulletinStatus status) {
		return switch (this) {
			case missing -> switch (status) {
				case missing -> 0;
				case draft, submitted, published, updated, resubmitted, republished -> -1;
			};
			case draft -> switch (status) {
				case missing -> 1;
				case draft -> 0;
				case updated, submitted, published, resubmitted, republished -> -1;
			};
			case submitted -> switch (status) {
				case missing, draft, updated -> 1;
				case submitted -> 0;
				case resubmitted, published, republished -> -1;
			};
			case published -> switch (status) {
				case missing, draft, updated, submitted, resubmitted -> 1;
				case published -> 0;
				case republished -> -1;
			};
			case updated -> switch (status) {
				case missing, draft -> 1;
				case updated -> 0;
				case submitted, published, resubmitted, republished -> -1;
			};
			case resubmitted -> switch (status) {
				case missing, draft, updated, submitted -> 1;
				case resubmitted -> 0;
				case published, republished -> -1;
			};
			case republished -> switch (status) {
				case missing, draft, updated, submitted, resubmitted, published -> 1;
				case republished -> 0;
			};
		};
	}
}
