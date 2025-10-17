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
        return switch (this) {
            case avActivityHighlights -> "avalancheActivityHighlights";
            case avActivityComment -> "avalancheActivityComment";
            case synopsisHighlights -> "wxSynopsisHighlights";
            case synopsisComment -> "wxSynopsisComment";
            case snowpackStructureHighlights -> "snowpackStructureHighlights";
            case snowpackStructureComment -> "snowpackStructureComment";
            case travelAdvisoryHighlights -> "travelAdvisoryHighlights";
            case travelAdvisoryComment -> "travelAdvisoryComment";
            case tendencyComment -> "tendencyComment";
            case highlights -> "highlights";
            case generalHeadlineComment -> "generalHeadlineComment";
		};
	}
}
