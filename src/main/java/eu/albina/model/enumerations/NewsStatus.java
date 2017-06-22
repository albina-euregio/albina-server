package eu.albina.model.enumerations;

public enum NewsStatus {
	draft, pending, published;

	public static NewsStatus fromString(String text) {
		if (text != null) {
			for (NewsStatus type : NewsStatus.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
