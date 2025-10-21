// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

public enum BulletinStatus {
	republished, resubmitted, updated, published, submitted, draft, missing;

	public static boolean isDraftOrUpdated(BulletinStatus status) {
		return status == draft || status == updated;
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
