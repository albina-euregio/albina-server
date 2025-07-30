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

import java.util.Arrays;

public enum TextPart {
	highlights, synopsisHighlights, synopsisComment, avActivityHighlights, avActivityComment, snowpackStructureHighlights, snowpackStructureComment, travelAdvisoryHighlights, travelAdvisoryComment, tendencyComment, generalHeadlineComment;

	public static TextPart fromString(String text) {
		if (text != null) {
			return Arrays.stream(TextPart.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlv6String() {
		switch (this) {
		case avActivityHighlights:
			return "avalancheActivityHighlights";
		case avActivityComment:
			return "avalancheActivityComment";
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
		case generalHeadlineComment:
			return "generalHeadlineComment";

		default:
			return null;
		}
	}
}
