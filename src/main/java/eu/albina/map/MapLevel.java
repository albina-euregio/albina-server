package eu.albina.map;

enum MapLevel {
	thumbnail(30), standard(160), overlay(200);

	MapLevel(int width) {
		this.width = width;
	}

	final int width;

	public String toString() {
		switch (this) {
		case thumbnail:
			return "thumbnail";
		case standard:
			return "map";
		case overlay:
			return "overlay";

		default:
			return null;
		}
	}
}
