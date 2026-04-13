// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Comparator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum BulletinStatus {
	republished, resubmitted, updated, published, submitted, draft, missing;

	private static final Logger logger = LoggerFactory.getLogger(BulletinStatus.class);

	public static final String COLUMN_DEFINITION = "enum ('republished', 'resubmitted', 'updated', 'published', 'submitted', 'draft', 'missing')";

	public static final Set<BulletinStatus> PUBLISHED_OR_REPUBLISHED = Set.of(published, republished);

	public static boolean isDraftOrUpdated(BulletinStatus status) {
		return status == draft || status == updated;
	}

	public static boolean isPublishedOrRepublished(BulletinStatus status) {
		return status == published || status == republished;
	}

	public BulletinStatus publishReport() {
		return switch (this) {
			case missing -> {
				logger.warn("Bulletins have to be created first!");
				yield missing;
			}
			case draft -> {
				logger.warn("Bulletins have to be submitted first!");
				yield draft;
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
			case republished, resubmitted, updated, published -> updated;
			case missing, draft, submitted -> draft;
		};
	}

	static final Comparator<BulletinStatus> COMPARATOR = Comparator.comparingInt(s -> switch (s) {
		case missing -> 0;
		case draft -> 1;
		case updated -> 2;
		case submitted -> 3;
		case resubmitted -> 4;
		case published -> 5;
		case republished -> 6;
	});

	public int comparePublicationStatus(BulletinStatus status) {
		return COMPARATOR.compare(this, status);
	}
}
