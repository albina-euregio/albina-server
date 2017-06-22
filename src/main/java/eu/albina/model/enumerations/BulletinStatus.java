package eu.albina.model.enumerations;

public enum BulletinStatus {
	obsolete, published, draft, missing;

	public static BulletinStatus fromString(String text) {
		if (text != null) {
			for (BulletinStatus type : BulletinStatus.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
