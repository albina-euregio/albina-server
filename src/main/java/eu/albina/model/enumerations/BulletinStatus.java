/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.model.enumerations;

public enum BulletinStatus {
	republished, resubmitted, updated, published, submitted, draft, missing;

	public static BulletinStatus fromString(String text) {
		if (text != null) {
			for (BulletinStatus type : BulletinStatus.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
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
