package eu.albina.map;

import eu.albina.model.AvalancheBulletin;
import eu.albina.util.GlobalVariables;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

enum MapType {
	fullmap,
	overlay,
	tyrol(GlobalVariables.codeTyrol, 1452000, 1116000, 6053000, 5829000),
	southtyrol(GlobalVariables.codeSouthTyrol, 1400000, 1145000, 5939000, 5769000),
	trentino(GlobalVariables.codeTrentino, 1358000, 1133000, 5842000, 5692000),
	fullmap_small;

	MapType() {
		this(null, 1464000, 1104000, 6047000, 5687000);
	}

	MapType(String region, int xmax, int xmin, int ymax, int ymin) {
		this.region = region;
		this.xmax = xmax;
		this.xmin = xmin;
		this.ymax = ymax;
		this.ymin = ymin;
	}

	final String region;
	final int xmax;
	final int xmin;
	final int ymax;
	final int ymin;

	double aspectRatio() {
		return ((double) xmax - (double) xmin) / ((double) ymax - (double) ymin);
	}

	String filename() {
		switch (this) {
			case fullmap:
				return "albina_map";
			case overlay:
				return "overlay";
			case tyrol:
			case southtyrol:
			case trentino:
				return name() + "_map";
			case fullmap_small:
				return "albina_thumbnail";
			default:
				return null;
		}
	}

	String filename(DaytimeDependency daytimeDependency, AvalancheBulletin bulletin, boolean grayscale, String format) {
		StringBuilder sb = new StringBuilder();
		if (bulletin == null) {
			sb.append(daytimeDependency.name());
			sb.append("_");
			sb.append(this.filename());
		} else {
			sb.append(bulletin.getId());
			sb.append(DaytimeDependency.pm.equals(daytimeDependency) ? "_PM" : "");
		}

		if (grayscale)
			sb.append("_bw");

		sb.append(".");
		sb.append(format);
		return sb.toString();
	}

	static Optional<MapType> forRegion(String region) {
		return Arrays.stream(MapType.values()).filter(m -> Objects.equals(m.region, region)).findFirst();
	}
}
