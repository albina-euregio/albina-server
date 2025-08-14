// SPDX-License-Identifier: AGPL-3.0-or-later
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
