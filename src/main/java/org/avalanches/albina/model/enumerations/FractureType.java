package org.avalanches.albina.model.enumerations;

public enum FractureType {
	fullBreak, partialBreak, noBreak;

	public static FractureType fromString(String text) {
		if (text != null) {
			for (FractureType type : FractureType.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
