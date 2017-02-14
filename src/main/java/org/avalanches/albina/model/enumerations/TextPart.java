package org.avalanches.albina.model.enumerations;

public enum TextPart {
	avalancheSituationHighlight, avalancheSituationComment, activityHighlight, activityComment, synopsisHighlight, synopsisComment, snowpackStructureHighlight, snowpackStructureComment, travelAdvisoryHighlight, travelAdvisoryComment, tendencyComment;

	public static TextPart fromString(String text) {
		if (text != null) {
			for (TextPart type : TextPart.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
