package org.avalanches.albina.model.enumerations;

public enum GrainShape {
	PP, PPgp, MM, DF, RG, FC, FCxr, DH, SH, MF, MFcr, IF;

	public static GrainShape fromString(String text) {
		if (text != null) {
			for (GrainShape type : GrainShape.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
