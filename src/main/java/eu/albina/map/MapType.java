package eu.albina.map;

import com.google.common.io.Resources;

import eu.albina.controller.RegionController;
import eu.albina.controller.ServerInstanceController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.util.GlobalVariables;

import java.util.*;

enum MapType {
	// TODO already exists in region class
	euregio(GlobalVariables.codeEuregio, 1464000, 1104000, 6047000, 5687000),
	aran(GlobalVariables.codeAran, 120500, 66200, 5266900, 5215700),
	tyrol(GlobalVariables.codeTyrol, 1452000, 1116000, 6053000, 5829000),
	southtyrol(GlobalVariables.codeSouthTyrol, 1400000, 1145000, 5939000, 5769000),
	trentino(GlobalVariables.codeTrentino, 1358000, 1133000, 5842000, 5692000);

	/**
	 * Bounding box in https://epsg.io/3395
	 */
	MapType(String region, int xmax, int xmin, int ymax, int ymin) {
		this.region = region;
		this.xmax = xmax;
		this.xmin = xmin;
		this.ymax = ymax;
		this.ymin = ymin;
	}

	// TODO already exists in region class
	final String region;
	final int xmax;
	final int xmin;
	final int ymax;
	final int ymin;

	public static Collection<MapType> forGlobalVariablesPublishBulletins() {
		final EnumSet<MapType> mapTypes = EnumSet.noneOf(MapType.class);

		if (RegionController.getInstance().getRegion(GlobalVariables.codeTyrol).isPublishBulletins()) {
			mapTypes.add(euregio);
			mapTypes.add(tyrol);
		}
		if (RegionController.getInstance().getRegion(GlobalVariables.codeSouthTyrol).isPublishBulletins()) {
			mapTypes.add(euregio);
			mapTypes.add(southtyrol);
		}
		if (RegionController.getInstance().getRegion(GlobalVariables.codeTrentino).isPublishBulletins()) {
			mapTypes.add(euregio);
			mapTypes.add(trentino);
		}
		if (RegionController.getInstance().getRegion(GlobalVariables.codeAran).isPublishBulletins()) {
			mapTypes.add(aran);
		}
		return mapTypes;
	}

	String geodata() {
		if (this == MapType.aran) {
			return ServerInstanceController.getInstance().getLocalServerInstance().getMapProductionUrl() + "geodata.Aran/";
		} else {
			return ServerInstanceController.getInstance().getLocalServerInstance().getMapProductionUrl() + "geodata.Euregio/";
		}
	}

	String realm() {
		if (this == MapType.aran) {
			return "Aran";
		} else {
			return "Euregio";
		}
	}

	String logo(MapLevel mapLevel, boolean grayscale) {
		if (!MapLevel.standard.equals(mapLevel)) {
			return "";
		} else if (this == MapType.aran) {
			return grayscale
				? Resources.getResource("images/logo/grey/lauegi_map.png").toString()
				: Resources.getResource("images/logo/color/lauegi_map.png").toString();
		} else if (this == MapType.euregio) {
			return grayscale
				? Resources.getResource("images/logo/grey/euregio_map.png").toString()
				: Resources.getResource("images/logo/color/euregio_map.png").toString();
		} else {
			return "";
		}
	}

	double width(MapLevel mapLevel) {
		return mapLevel.width;
	}

	double height(MapLevel mapLevel) {
		return mapLevel.width / aspectRatio();
	}

	double aspectRatio() {
		return ((double) xmax - (double) xmin) / ((double) ymax - (double) ymin);
	}

	String filename(MapLevel mapLevel) {
		switch (this) {
			case tyrol:
			case southtyrol:
			case trentino:
				return name() + "_map";
			case euregio:
				switch (mapLevel) {
					case standard:
						return "albina_map";
					case thumbnail:
						return "albina_thumbnail";
					case overlay:
						return "overlay";
				}
			case aran:
				switch (mapLevel) {
					case standard:
						return "aran_map";
					case thumbnail:
						return "aran_thumbnail";
					case overlay:
						return "aran_overlay";
				}
			default:
				return null;
		}
	}

	String filename(MapLevel mapLevel, DaytimeDependency daytimeDependency, AvalancheBulletin bulletin, boolean grayscale, MapImageFormat format) {
		StringBuilder sb = new StringBuilder();
		if (bulletin == null) {
			sb.append(daytimeDependency.name());
			sb.append("_");
			sb.append(this.filename(mapLevel));
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
