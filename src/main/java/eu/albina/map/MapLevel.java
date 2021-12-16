package eu.albina.map;

enum MapLevel {
	thumbnail(30), standard(160), overlay(200);

	MapLevel(int width) {
		this.width = width;
	}

	final int width;

}
