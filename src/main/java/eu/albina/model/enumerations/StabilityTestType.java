package eu.albina.model.enumerations;

public enum StabilityTestType {
	CT, ECT, RB, PST;

	public static StabilityTestType fromString(String text) {
		if (text != null) {
			for (StabilityTestType type : StabilityTestType.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
