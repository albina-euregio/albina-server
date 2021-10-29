package eu.albina.map;

enum MapSize {
	thumbnail_map(30), standard_map(160), overlay(200);

	MapSize(int width) {
		this.width = width;
	}

	final int width;

	static MapSize of(MapType map) {
		return MapType.overlay.equals(map) ? overlay : MapType.fullmap_small.equals(map) ? thumbnail_map : standard_map;
	}
}
