package eu.albina.model.enumerations;

public enum Role {
	ADMIN, TRENTINO, SOUTH_TYROL, TYROL, STYRIA, OBSERVER;

	public static Role fromString(String text) {
		if (text != null) {
			for (Role role : Role.values()) {
				if (text.equalsIgnoreCase(role.toString()))
					return role;
			}
		}
		return null;
	}
}
