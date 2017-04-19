package eu.albina.model.enumerations;

public enum Status {
	missing, incomplete, complete;

	public static Status fromString(String text) {
		if (text != null) {
			for (Status type : Status.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
