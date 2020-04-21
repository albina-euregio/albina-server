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

public enum TextPart {
	highlights, synopsisHighlights, synopsisComment, avActivityHighlights, avActivityComment, snowpackStructureHighlights, snowpackStructureComment, travelAdvisoryHighlights, travelAdvisoryComment, tendencyComment;

	public static TextPart fromString(String text) {
		if (text != null) {
			for (TextPart type : TextPart.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}

	public String toCaamlString() {
		switch (this) {
		case avActivityHighlights:
			return "avActivityHighlights";
		case avActivityComment:
			return "avActivityComment";
		case synopsisHighlights:
			return "wxSynopsisHighlights";
		case synopsisComment:
			return "wxSynopsisComment";
		case snowpackStructureHighlights:
			return "snowpackStructureHighlights";
		case snowpackStructureComment:
			return "snowpackStructureComment";
		case travelAdvisoryHighlights:
			return "travelAdvisoryHighlights";
		case travelAdvisoryComment:
			return "travelAdvisoryComment";
		case tendencyComment:
			return "tendencyComment";
		case highlights:
			return "highlights";

		default:
			return null;
		}
	}
}
