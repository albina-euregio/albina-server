/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.map;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.script.SimpleBindings;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.common.io.Resources;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.GeoJson;
import eu.albina.util.GlobalVariables;
import org.mapyrus.Argument;
import org.mapyrus.ContextStack;
import org.mapyrus.FileOrURL;
import org.mapyrus.Interpreter;
import org.mapyrus.MapyrusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.Region;
import eu.albina.model.Regions;
import eu.albina.model.enumerations.DangerRating;

public interface MapUtil {

	Logger logger = LoggerFactory.getLogger(MapUtil.class);

	class AlbinaMapException extends RuntimeException {

		static final long serialVersionUID = 1L;

		public AlbinaMapException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	// REGION
	static String getOverviewMapFilename(String region, boolean isAfternoon, boolean hasDaytimeDependency,
			boolean grayscale) {
		final DaytimeDependency daytimeDependency = DaytimeDependency.of(isAfternoon, hasDaytimeDependency);
		return Map.forRegion(region).orElse(Map.fullmap).filename(daytimeDependency, null, grayscale, "jpg");
	}

	/**
	 * Create images of each map needed for the different products of
	 * avalanche.report. This consists of an overview map over the whole EUREGIO,
	 * maps for each province and detailed maps for each aggregated region.
	 *
	 * @param bulletins
	 *            The bulletins to create the maps from.
	 * @param regions
	 *            The regions for the bulletin
	 * @throws Exception
	 *             an error occurred during map production
	 */
	static void createDangerRatingMaps(List<AvalancheBulletin> bulletins, Regions regions, String publicationTime, boolean preview) throws Exception {
		final long start = System.currentTimeMillis();
		logger.info("Creating danger rating maps for {} using {}", AlbinaUtil.getValidityDateString(bulletins),
				GlobalVariables.getMapProductionUrl());
		try {
			createMapyrusMaps(bulletins, regions, publicationTime, preview);
		} catch (Exception ex) {
			logger.error("Failed to create mapyrus maps", ex);
			throw ex;
		}
		logger.info("Creating danger rating maps done in {} ms", System.currentTimeMillis() - start);
	}

	static SimpleBindings createMayrusBindings(List<AvalancheBulletin> bulletins, DaytimeDependency daytimeDependency, boolean preview) {
		Table<String, String, Integer> bindings = TreeBasedTable.create();
		int index = 0;
		for (AvalancheBulletin bulletin : bulletins) {
			Iterable<String> regions = preview ? bulletin.getPublishedAndSavedRegions() : bulletin.getPublishedRegions();
			for (String region : regions) {
				AvalancheBulletinDaytimeDescription description = getBulletinDaytimeDescription(bulletin, daytimeDependency);
				bindings.put("bul_id_h", region + "-h", index);
				bindings.put("bul_id_l", region + "-l", index);
				bindings.put("danger_h", region + "-h", getDangerRatingInt(description, true));
				bindings.put("danger_l", region + "-l", getDangerRatingInt(description, false));
				bindings.put("elevation_h", region + "-h", description.getElevation());
			}
			index++;
		}
		SimpleBindings simpleBindings = new SimpleBindings();
		bindings.rowMap().forEach((key, stringObjectMap) -> {
			Argument argument = new Argument();
			stringObjectMap.forEach((region, value) -> argument.addHashMapEntry(region, new Argument(value)));
			simpleBindings.put(key, argument);
		});
		return simpleBindings;
	}

	static AvalancheBulletinDaytimeDescription getBulletinDaytimeDescription(AvalancheBulletin bulletin,
			DaytimeDependency daytimeDependency) {
		return bulletin.isHasDaytimeDependency() && DaytimeDependency.pm.equals(daytimeDependency)
				? bulletin.getAfternoon()
				: bulletin.getForenoon();
	}

	static String getDangerRatingString(AvalancheBulletinDaytimeDescription description, boolean above) {
		if (description.isHasElevationDependency() && !above) {
			return DangerRating.getString(description.getDangerRatingBelow());
		} else {
			return DangerRating.getString(description.getDangerRatingAbove());
		}
	}

	static int getDangerRatingInt(AvalancheBulletinDaytimeDescription description, boolean above) {
		if (description.isHasElevationDependency() && !above) {
			return DangerRating.getInt(description.getDangerRatingBelow());
		} else {
			return DangerRating.getInt(description.getDangerRatingAbove());
		}
	}

	enum Map {
		fullmap, overlay, tyrol(GlobalVariables.codeTyrol, 1452000, 1116000, 6053000, 5829000), southtyrol(
				GlobalVariables.codeSouthTyrol, 1400000, 1145000, 5939000,
				5769000), trentino(GlobalVariables.codeTrentino, 1358000, 1133000, 5842000, 5692000), fullmap_small;

		Map() {
			this(null, 1464000, 1104000, 6047000, 5687000);
		}

		Map(String region, int xmax, int xmin, int ymax, int ymin) {
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

		static Optional<Map> forRegion(String region) {
			return Arrays.stream(values()).filter(m -> Objects.equals(m.region, region)).findFirst();
		}
	}

	enum MapSize {
		thumbnail_map(30), standard_map(160), overlay(200);

		MapSize(int width) {
			this.width = width;
		}

		final int width;

		static MapSize of(Map map) {
			return Map.overlay.equals(map) ? overlay : Map.fullmap_small.equals(map) ? thumbnail_map : standard_map;
		}
	}

	enum DaytimeDependency {
		/**
		 * full day
		 */
		fd, am, pm;

		static DaytimeDependency of(boolean isAfternoon, boolean hasDaytimeDependency) {
			return !hasDaytimeDependency ? fd
					: isAfternoon ? pm : am;
		}
	}

	class MapyrusInterpreter {
		final Interpreter interpreter = new Interpreter();
		final ContextStack context = new ContextStack();

		MapyrusInterpreter(SimpleBindings bindings) {
			context.setBindings(bindings);
		}

		void interpret(FileOrURL fileOrURL) throws MapyrusException, IOException, InterruptedException {
			interpreter.interpret(context, fileOrURL, System.in, System.out);
		}

		void interpret(URL resource) throws MapyrusException, IOException, InterruptedException {
			interpret(new FileOrURL(resource.toString()));
		}
	}

	static void createMapyrusMaps(List<AvalancheBulletin> bulletins, Regions regions, String publicationTime, boolean preview) {
		Path outputDirectory;
		if (preview) {
		 	outputDirectory = Paths.get(GlobalVariables.getTmpMapsPath(),
				AlbinaUtil.getValidityDateString(bulletins), publicationTime);
		} else {
			outputDirectory = Paths.get(GlobalVariables.getMapsPath(),
				AlbinaUtil.getValidityDateString(bulletins), publicationTime);
		}

		try {
			logger.info("Creating directory {}", outputDirectory);
			Files.createDirectories(outputDirectory);
		} catch (IOException ex) {
			throw new AlbinaMapException("Failed to create output directory", ex);
		}
		final boolean hasDaytimeDependency = bulletins.stream().anyMatch(AvalancheBulletin::isHasDaytimeDependency);
		for (DaytimeDependency daytimeDependency : hasDaytimeDependency
				? EnumSet.of(DaytimeDependency.am, DaytimeDependency.pm)
				: EnumSet.of(DaytimeDependency.fd)) {
			try {
				final Path regionFile = outputDirectory.resolve(daytimeDependency + "_regions.json");
				logger.info("Creating region file {}", regionFile);
				createBulletinRegions(bulletins, daytimeDependency, regionFile, regions, preview);
			} catch (IOException | AlbinaException ex) {
				throw new AlbinaMapException("Failed to create region file", ex);
			}
			try {
				final SimpleBindings bindings = createMayrusBindings(bulletins, daytimeDependency, preview);
				if (!preview) {
					for (Map map : Map.values()) {
						createMapyrusMaps(map, daytimeDependency, null, 0, false, bindings, outputDirectory, preview);
						createMapyrusMaps(map, daytimeDependency, null, 0, true, bindings, outputDirectory, preview);
					}
				} else {
					createMapyrusMaps(Map.fullmap, daytimeDependency, null, 0, false, bindings, outputDirectory, preview);
				}
				for (int i = 0; i < bulletins.size(); i++) {
					final AvalancheBulletin bulletin = bulletins.get(i);
					if (DaytimeDependency.pm.equals(daytimeDependency) && !bulletin.isHasDaytimeDependency()) {
						continue;
					}
					createMapyrusMaps(Map.fullmap_small, daytimeDependency, bulletin, i, false, bindings, outputDirectory, preview);
					if (!preview)
						createMapyrusMaps(Map.fullmap_small, daytimeDependency, bulletin, i, true, bindings, outputDirectory, preview);
				}
			} catch (IOException | MapyrusException | InterruptedException ex) {
				throw new AlbinaMapException("Failed to create mapyrus maps", ex);
			}
		}
	}

	static void createMapyrusMaps(Map map, DaytimeDependency daytimeDependency, AvalancheBulletin bulletin, int bulletinIndex,
								  boolean grayscale, SimpleBindings dangerBindings, Path outputDirectory, boolean preview) throws IOException, MapyrusException, InterruptedException {

		final MapSize size = MapSize.of(map);
		final String mapProductionUrl = GlobalVariables.getMapProductionUrl();
		final Path outputFile = outputDirectory.resolve(map.filename(daytimeDependency, bulletin, grayscale, "pdf"));
		final Path tempDirectory = Files.createTempDirectory("mapyrus");
		final SimpleBindings bindings = new SimpleBindings(new TreeMap<>());
		bindings.put("xmax", map.xmax);
		bindings.put("xmin", map.xmin);
		bindings.put("ymax", map.ymax);
		bindings.put("ymin", map.ymin);
		bindings.put("image_type", "pdf");
		bindings.put("mapFile", outputFile);
		bindings.put("pagesize_x", size.width);
		bindings.put("pagesize_y", size.width / map.aspectRatio());
		bindings.put("map_xsize", size.width);
		bindings.put("working_dir", tempDirectory + "/");
		bindings.put("geodata_dir", mapProductionUrl + "geodata/");
		bindings.put("image_dir", mapProductionUrl + "images/");
		bindings.put("region", "Euregio");
		bindings.put("level", size.ordinal() + 1);
		bindings.put("colormode", grayscale ? "bw" : "col");
		bindings.put("dynamic_region", bulletin != null ? "one" : "all");
		bindings.put("language", "en");
		bindings.put("scalebar", Map.overlay.equals(map) ? "off" : "on");
		bindings.put("copyright", Map.overlay.equals(map) ? "off" : "on");
		bindings.put("interreg", Map.fullmap.equals(map) ? "on" : "off");
		bindings.put("logo", Map.fullmap.equals(map) ? "on" : "off");
		bindings.put("bulletin_id", bulletin != null ? bulletinIndex : map.name());
		bindings.putAll(dangerBindings);

		final String otf_mapyrus = String.format("let otf_mapyrus = \" otffiles=%s,%s,%s,%s,%s,%s \"",
			Resources.getResource("fonts/open-sans/OpenSans.otf").getFile(),
			Resources.getResource("fonts/open-sans/OpenSans-Italic.otf").getFile(),
			Resources.getResource("fonts/open-sans/OpenSans-Bold.otf").getFile(),
			Resources.getResource("fonts/open-sans/OpenSans-BoldItalic.otf").getFile(),
			Resources.getResource("fonts/open-sans/OpenSans-Semibold.otf").getFile(),
			Resources.getResource("fonts/open-sans/OpenSans-SemiboldItalic.otf").getFile());

		logger.info("Creating map {} using {} with bindings {}", outputFile, dangerBindings, bindings);
		final MapyrusInterpreter mapyrus = new MapyrusInterpreter(bindings);
		mapyrus.interpret(new FileOrURL(new StringReader(otf_mapyrus), "otf.mapyrus"));
		mapyrus.interpret(Resources.getResource("mapyrus/fontdefinition.mapyrus"));
		mapyrus.interpret(Resources.getResource("mapyrus/albina_functions.mapyrus"));
		mapyrus.interpret(Resources.getResource("mapyrus/albina_styles.mapyrus"));
		if (Map.overlay.equals(map)) {
			mapyrus.interpret(Resources.getResource("mapyrus/albina_overlaymap.mapyrus"));
		} else {
			if (bulletin != null) {
				AvalancheBulletinDaytimeDescription description = getBulletinDaytimeDescription(bulletin, daytimeDependency);
				mapyrus.context.getBindings().put("elevation_level", description.getElevation());
				mapyrus.context.getBindings().put("danger_rating_low", getDangerRatingString(description, false));
				mapyrus.context.getBindings().put("danger_rating_high", getDangerRatingString(description, true));
				mapyrus.context.getBindings().put("problem_icon_l", "0");
				mapyrus.context.getBindings().put("problem_icon_h", "0");
			}
			mapyrus.interpret(Resources.getResource("mapyrus/albina_drawmap.mapyrus"));
		}
		deleteDirectoryWithContents(tempDirectory);

		final int dpi = 300;
		final String outputFileJpg = outputFile.toString().replaceFirst("pdf$", "jpg");
		final String outputFilePng = outputFile.toString().replaceFirst("pdf$", "png");
		final String outputFileWebp = outputFile.toString().replaceFirst("pdf$", "webp");

		// convert to png
		logger.info("Converting {} to {}", outputFile, outputFilePng);
		new ProcessBuilder("gs", "-sDEVICE=png16m", "-dTextAlphaBits=4", "-dGraphicsAlphaBits=4", "-r" + dpi, "-o",
				outputFilePng, outputFile.toString()).inheritIO().start().waitFor();
		// create transparency
		if (Map.overlay.equals(map)) {
			logger.info("Creating transparency for {}", outputFilePng);
			new ProcessBuilder("convert", "-transparent", "white", outputFilePng, outputFilePng).inheritIO().start()
					.waitFor();
		}

		// convert to jpg
		logger.info("Converting {} to {}", outputFile, outputFileJpg);
		new ProcessBuilder("convert", outputFilePng, outputFileJpg).inheritIO().start().waitFor();
		if (DaytimeDependency.pm.equals(daytimeDependency) && bulletin == null) {
			// create combined am/pm maps
			final String amFile = outputDirectory.resolve(map.filename(DaytimeDependency.am, null, grayscale, "jpg")).toString();
			final String pmFile = outputDirectory.resolve(map.filename(DaytimeDependency.pm, null, grayscale, "jpg")).toString();
			final String fdFile = outputDirectory.resolve(map.filename(DaytimeDependency.fd, null, grayscale, "jpg")).toString();
			logger.info("Combining {} and {} to {}", amFile, pmFile, fdFile);
			new ProcessBuilder("convert", "+append", amFile, pmFile, fdFile).inheritIO().start().waitFor();
		}

		if (!preview) {
			// convert to webp
			logger.info("Converting {} to {}", outputFilePng, outputFileWebp);
			new ProcessBuilder("cwebp", outputFilePng, "-o", outputFileWebp).inheritIO().start().waitFor();
		}
	}

	static void deleteDirectoryWithContents(Path directory) throws IOException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
			for (Path path : directoryStream) {
				Files.delete(path);
			}
		}
		Files.delete(directory);
	}

	static void createBulletinRegions(List<AvalancheBulletin> bulletins, DaytimeDependency daytimeDependency,
			Path regionFile, Regions regions, boolean preview) throws IOException, AlbinaException {
		final GeoJson.FeatureCollection featureCollection = new GeoJson.FeatureCollection();
		if (bulletins.get(0).getPublicationDate() != null) {
			featureCollection.properties.put("creation_date", DateTimeFormatter.ISO_INSTANT.format(bulletins.get(0).getPublicationDate()));
		}
		featureCollection.properties.put("valid_date", bulletins.get(0).getValidityDateString());
		featureCollection.properties.put("valid_daytime", daytimeDependency.name());
		for (AvalancheBulletin bulletin : bulletins) {
			final GeoJson.Feature feature = new GeoJson.Feature();
			final AvalancheBulletinDaytimeDescription description = getBulletinDaytimeDescription(bulletin,
					daytimeDependency);
			feature.properties.put("bid", bulletin.getId());
			feature.properties.put("daytime", daytimeDependency.name());
			feature.properties.put("elevation", description.getElevation());
			feature.properties.put("dl_hi", getDangerRatingString(description, true));
			feature.properties.put("dl_lo", getDangerRatingString(description, false));
			feature.properties.put("problem_1", getAvalancheSituationString(description.getAvalancheSituation1()));
			feature.properties.put("problem_2", getAvalancheSituationString(description.getAvalancheSituation2()));
			final double bufferDistance = 1e-4;
			feature.geometry = regions.getRegionsForBulletin(bulletin, preview).map(Region::getPolygon)
					// use buffer to avoid artifacts when building polygon union
					.map(polygon -> BufferOp.bufferOp(polygon, bufferDistance))
					.collect(Collectors.collectingAndThen(Collectors.toList(), UnaryUnionOp::union));
			feature.geometry = BufferOp.bufferOp(feature.geometry, -bufferDistance);
			// round coordinates to 4 decimal digits in order to reduce the file size
			feature.geometry = GeometryPrecisionReducer.reduce(feature.geometry, new PrecisionModel(1e4));
			featureCollection.features.add(feature);
		}
		new ObjectMapper().writeValue(regionFile.toFile(), featureCollection);
	}

	static String getAvalancheSituationString(AvalancheSituation avalancheSituation) {
		return Optional.ofNullable(avalancheSituation).map(AvalancheSituation::getAvalancheSituation)
				.map(eu.albina.model.enumerations.AvalancheSituation::toCaamlv5String).orElse("false");
	}
}
