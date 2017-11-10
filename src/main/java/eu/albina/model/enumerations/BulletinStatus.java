package eu.albina.model.enumerations;

public enum BulletinStatus {
	missing, draft, submitted, published, updated, resubmitted, republished, obsolete;

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
