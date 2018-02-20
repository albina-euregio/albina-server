package eu.albina.model.enumerations;

public enum TextPart {
	synopsisHighlights, synopsisComment, avActivityHighlights, avActivityComment, snowpackStructureHighlights, snowpackStructureComment, travelAdvisoryHighlights, travelAdvisoryComment, tendencyComment;

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

		default:
			return null;
		}
	}
}
