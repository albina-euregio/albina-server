package org.avalanches.ais.model.enumerations;

public enum TriggerType {
	high_load, low_load, spontanous;

	public static TriggerType fromString(String text) {
		if (text != null) {
			for (TriggerType type : TriggerType.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
