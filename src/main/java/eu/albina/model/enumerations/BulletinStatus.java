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

import java.util.Comparator;

public enum BulletinStatus {
	missing, draft, updated, submitted, resubmitted, published, republished;

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
		return Comparator.<BulletinStatus>comparingInt(Enum::ordinal).compare(this, status);
	}
}
