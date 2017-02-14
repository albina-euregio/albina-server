package org.avalanches.ais.model.enumerations;

public enum DangerZone {
	above, below;

	public static DangerZone fromString(String text) {
		if (text != null) {
			for (DangerZone type : DangerZone.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
