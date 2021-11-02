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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import org.mapyrus.FileOrURL;
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
import eu.albina.model.Region;
import eu.albina.model.Regions;
import eu.albina.model.enumerations.DangerRating;

public interface MapUtil {

	Logger logger = LoggerFactory.getLogger(MapUtil.class);

	// REGION
	static String getOverviewMapFilename(String region, boolean isAfternoon, boolean hasDaytimeDependency,
			boolean grayscale) {
		final DaytimeDependency daytimeDependency = DaytimeDependency.of(isAfternoon, hasDaytimeDependency);
		return MapType.forRegion(region).orElse(MapType.fullmap).filename(daytimeDependency, null, grayscale, MapImageFormat.jpg);
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
				AvalancheBulletinDaytimeDescription description = daytimeDependency.getBulletinDaytimeDescription(bulletin);
				bindings.put("bul_id_h", region + "-h", index);
				bindings.put("bul_id_l", region + "-l", index);
				bindings.put("danger_h", region + "-h", DangerRating.getInt(description.dangerRating(true)));
				bindings.put("danger_l", region + "-l", DangerRating.getInt(description.dangerRating(false)));
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
		for (DaytimeDependency daytimeDependency : DaytimeDependency.of(bulletins)) {
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
					for (MapType map : MapType.values()) {
						createMapyrusMaps(map, daytimeDependency, null, 0, false, bindings, outputDirectory, preview);
						createMapyrusMaps(map, daytimeDependency, null, 0, true, bindings, outputDirectory, preview);
					}
				} else {
					createMapyrusMaps(MapType.fullmap, daytimeDependency, null, 0, false, bindings, outputDirectory, preview);
				}
				for (int i = 0; i < bulletins.size(); i++) {
					final AvalancheBulletin bulletin = bulletins.get(i);
					if (DaytimeDependency.pm.equals(daytimeDependency) && !bulletin.isHasDaytimeDependency()) {
						continue;
					}
					createMapyrusMaps(MapType.fullmap_small, daytimeDependency, bulletin, i, false, bindings, outputDirectory, preview);
					if (!preview)
						createMapyrusMaps(MapType.fullmap_small, daytimeDependency, bulletin, i, true, bindings, outputDirectory, preview);
				}
			} catch (IOException | MapyrusException | InterruptedException ex) {
				throw new AlbinaMapException("Failed to create mapyrus maps", ex);
			}
		}
	}

	static void createMapyrusMaps(MapType map, DaytimeDependency daytimeDependency, AvalancheBulletin bulletin, int bulletinIndex,
								  boolean grayscale, SimpleBindings dangerBindings, Path outputDirectory, boolean preview) throws IOException, MapyrusException, InterruptedException {

		final MapSize size = MapSize.of(map);
		final String mapProductionUrl = GlobalVariables.getMapProductionUrl();
		final Path outputFile = outputDirectory.resolve(map.filename(daytimeDependency, bulletin, grayscale, MapImageFormat.pdf));
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
		bindings.put("geodata_dir", mapProductionUrl + "geodata/");
		bindings.put("image_dir", mapProductionUrl + "images/");
		bindings.put("region", "Euregio");
		bindings.put("level", size.ordinal() + 1);
		bindings.put("colormode", grayscale ? "bw" : "col");
		bindings.put("dynamic_region", bulletin != null ? "one" : "all");
		bindings.put("scalebar", MapType.overlay.equals(map) ? "off" : "on");
		bindings.put("copyright", MapType.overlay.equals(map) ? "off" : "on");
		bindings.put("interreg", MapType.fullmap.equals(map) ? "on" : "off");
		bindings.put("logo", MapType.fullmap.equals(map) ? "on" : "off");
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
		if (MapType.overlay.equals(map)) {
			mapyrus.interpret(Resources.getResource("mapyrus/albina_overlaymap.mapyrus"));
		} else {
			if (bulletin != null) {
				AvalancheBulletinDaytimeDescription description = daytimeDependency.getBulletinDaytimeDescription(bulletin);
				mapyrus.context.getBindings().put("elevation_level", description.getElevation());
				mapyrus.context.getBindings().put("danger_rating_low", DangerRating.getString(description.dangerRating(false)));
				mapyrus.context.getBindings().put("danger_rating_high", DangerRating.getString(description.dangerRating(true)));
				mapyrus.context.getBindings().put("problem_icon_l", "0");
				mapyrus.context.getBindings().put("problem_icon_h", "0");
			}
			mapyrus.interpret(Resources.getResource("mapyrus/albina_drawmap.mapyrus"));
		}

		final Path outputFilePng = MapImageFormat.png.convertFrom(outputFile);
		if (MapType.overlay.equals(map)) {
			MapImageFormat.pngTransparent.convertFrom(outputFilePng);
		}

		MapImageFormat.jpg.convertFrom(outputFilePng);
		if (DaytimeDependency.pm.equals(daytimeDependency) && bulletin == null) {
			// create combined am/pm maps
			final String amFile = outputDirectory.resolve(map.filename(DaytimeDependency.am, null, grayscale, MapImageFormat.jpg)).toString();
			final String pmFile = outputDirectory.resolve(map.filename(DaytimeDependency.pm, null, grayscale, MapImageFormat.jpg)).toString();
			final String fdFile = outputDirectory.resolve(map.filename(DaytimeDependency.fd, null, grayscale, MapImageFormat.jpg)).toString();
			logger.info("Combining {} and {} to {}", amFile, pmFile, fdFile);
			new ProcessBuilder("convert", "+append", amFile, pmFile, fdFile).inheritIO().start().waitFor();
		}

		if (!preview) {
			MapImageFormat.webp.convertFrom(outputFilePng);
		}
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
			final AvalancheBulletinDaytimeDescription description = daytimeDependency.getBulletinDaytimeDescription(bulletin);
			feature.properties.put("bid", bulletin.getId());
			feature.properties.put("daytime", daytimeDependency.name());
			feature.properties.put("elevation", description.getElevation());
			feature.properties.put("dl_hi", DangerRating.getString(description.dangerRating(true)));
			feature.properties.put("dl_lo", DangerRating.getString(description.dangerRating(false)));
			feature.properties.put("problem_1", eu.albina.model.enumerations.AvalancheSituation.toCaamlv5String(description.getAvalancheSituation1()));
			feature.properties.put("problem_2", eu.albina.model.enumerations.AvalancheSituation.toCaamlv5String(description.getAvalancheSituation2()));
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

}
