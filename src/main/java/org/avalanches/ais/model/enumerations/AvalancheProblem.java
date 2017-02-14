package org.avalanches.ais.model.enumerations;

public enum AvalancheProblem {
	new_snow, wind_drifted_snow, old_snow, wet_snow, gliding_snow;

	public static AvalancheProblem fromString(String text) {
		if (text != null) {
			for (AvalancheProblem type : AvalancheProblem.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
