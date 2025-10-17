package eu.albina.map;

public enum MapLevel {
	thumbnail(30), standard(160), overlay(200);

	MapLevel(int width) {
		this.width = width;
	}

	final int width;

	public String toString() {
		return switch (this) {
			case thumbnail -> "thumbnail";
			case standard -> "map";
			case overlay -> "overlay";
		};
	}
}
