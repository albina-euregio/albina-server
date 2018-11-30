package eu.albina.model.enumerations;

public enum HazardSiteDistribution {
	single, some, many, many_most, moderately_steep;

	public static HazardSiteDistribution fromString(String text) {
		if (text != null) {
			for (HazardSiteDistribution type : HazardSiteDistribution.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
