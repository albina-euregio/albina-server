package org.avalanches.ais.model.enumerations;

public enum AvalancheType {
	slab, loose, glide;

	public static AvalancheType fromString(String text) {
		if (text != null) {
			for (AvalancheType type : AvalancheType.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
