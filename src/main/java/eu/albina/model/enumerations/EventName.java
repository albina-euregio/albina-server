package eu.albina.model.enumerations;

public enum EventName {
	bulletinUpdate, chatEvent, lockBulletin, unlockBulletin, lockRegion, unlockRegion;

	public static EventName fromString(String text) {
		if (text != null) {
			for (EventName type : EventName.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
