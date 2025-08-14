// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum BulletinStatus {
	republished, resubmitted, updated, published, submitted, draft, missing;

	public static boolean isDraftOrUpdated(BulletinStatus status) {
		return status == draft || status == updated;
	}

	public static BulletinStatus fromString(String text) {
		if (text != null) {
			return Arrays.stream(BulletinStatus.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public int comparePublicationStatus(BulletinStatus status) {
		switch (this) {
		case missing:
			switch (status) {
			case missing:
				return 0;
			case draft:
			case submitted:
			case published:
			case updated:
			case resubmitted:
			case republished:
				return -1;
			default:
				return 1;
			}
		case draft:
			switch (status) {
			case missing:
				return 1;
			case draft:
				return 0;
			case updated:
			case submitted:
			case published:
			case resubmitted:
			case republished:
				return -1;
			default:
				return 1;
			}
		case submitted:
			switch (status) {
			case missing:
			case draft:
			case updated:
				return 1;
			case submitted:
				return 0;
			case resubmitted:
			case published:
			case republished:
				return -1;
			default:
				return 1;
			}
		case published:
			switch (status) {
			case missing:
			case draft:
			case updated:
			case submitted:
			case resubmitted:
				return 1;
			case published:
				return 0;
			case republished:
				return -1;
			default:
				return 1;
			}
		case updated:
			switch (status) {
			case missing:
			case draft:
				return 1;
			case updated:
				return 0;
			case submitted:
			case published:
			case resubmitted:
			case republished:
				return -1;
			default:
				return 1;
			}
		case resubmitted:
			switch (status) {
			case missing:
			case draft:
			case updated:
			case submitted:
				return 1;
			case resubmitted:
				return 0;
			case published:
			case republished:
				return -1;
			default:
				return 1;
			}
		case republished:
			switch (status) {
			case missing:
			case draft:
			case updated:
			case submitted:
			case resubmitted:
			case published:
				return 1;
			case republished:
				return 0;
			default:
				return 1;
			}
		default:
			return -1;
		}
	}
}
