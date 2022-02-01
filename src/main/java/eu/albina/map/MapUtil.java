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
import java.util.*;

import javax.script.SimpleBindings;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.common.io.Resources;
import eu.albina.util.AlbinaUtil;

import org.mapyrus.Argument;
import org.mapyrus.FileOrURL;
import org.mapyrus.MapyrusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.MapProductionConfiguration;
import eu.albina.model.ServerInstance;
import eu.albina.model.Region;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.DaytimeDependency;

public interface MapUtil {

	Logger logger = LoggerFactory.getLogger(MapUtil.class);

	static String getOverviewMapFilename(Region region, DaytimeDependency daytimeDependency,
			boolean grayscale) {
		return MapUtil.filename(region, MapLevel.standard, daytimeDependency, null, grayscale, MapImageFormat.jpg);
	}

	static SimpleBindings createMayrusBindings(List<AvalancheBulletin> bulletins, DaytimeDependency daytimeDependency, boolean preview) {
		Table<String, String, Argument> bindings = TreeBasedTable.create();
		for (AvalancheBulletin bulletin : bulletins) {
			for (String region : bulletin.regions(preview)) {
				AvalancheBulletinDaytimeDescription description = daytimeDependency.getBulletinDaytimeDescription(bulletin);
				bindings.put("bulletin_ids", region + "-h", new Argument(Argument.STRING, bulletin.getId()));
				bindings.put("bulletin_ids", region + "-l", new Argument(Argument.STRING, bulletin.getId()));
				bindings.put("danger_h", region + "-h", new Argument(DangerRating.getInt(description.dangerRating(true))));
				bindings.put("danger_l", region + "-l", new Argument(DangerRating.getInt(description.dangerRating(false))));
				bindings.put("elevation_h", region + "-h", new Argument(description.getElevation()));
			}
		}
		SimpleBindings simpleBindings = new SimpleBindings();
		bindings.rowMap().forEach((key, stringObjectMap) -> {
			Argument argument = new Argument();
			stringObjectMap.forEach(argument::addHashMapEntry);
			simpleBindings.put(key, argument);
		});
		return simpleBindings;
	}

	static void createMapyrusMaps(List<AvalancheBulletin> bulletins, Region region, ServerInstance serverInstance) {
		final String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		final String publicationTime = AlbinaUtil.getPublicationTime(bulletins);
		final Path outputDirectory = Paths.get(serverInstance.getMapsPath(), validityDateString, publicationTime);
		createMapyrusMaps(bulletins, region, serverInstance, false, outputDirectory);
	}

	static void createMapyrusMaps(List<AvalancheBulletin> bulletins, Region region, ServerInstance serverInstance, boolean preview, Path outputDirectory) {
		try {
			logger.info("Creating directory {}", outputDirectory);
			Files.createDirectories(outputDirectory);
		} catch (IOException ex) {
			throw new AlbinaMapException("Failed to create output directory", ex);
		}

		for (DaytimeDependency daytimeDependency : DaytimeDependency.of(bulletins)) {
			try {
				final SimpleBindings bindings = createMayrusBindings(bulletins, daytimeDependency, preview);
				for (MapLevel mapLevel : MapLevel.values()) {
					createMapyrusMaps(region, serverInstance, mapLevel, daytimeDependency, null, false, bindings, outputDirectory, preview);
					createMapyrusMaps(region, serverInstance, mapLevel, daytimeDependency, null, true, bindings, outputDirectory, preview);
				}
				for (final AvalancheBulletin bulletin : bulletins) {
					if (DaytimeDependency.pm.equals(daytimeDependency) && !bulletin.isHasDaytimeDependency()) {
						continue;
					}
					createMapyrusMaps(region, serverInstance, MapLevel.thumbnail, daytimeDependency, bulletin, false, bindings, outputDirectory, preview);
					createMapyrusMaps(region, serverInstance, MapLevel.thumbnail, daytimeDependency, bulletin, true, bindings, outputDirectory, preview);
				}
			} catch (IOException | MapyrusException | InterruptedException ex) {
				throw new AlbinaMapException("Failed to create mapyrus maps", ex);
			}
		}
	}

	static void createMapyrusMaps(Region region, ServerInstance serverInstance, MapLevel mapLevel, DaytimeDependency daytimeDependency, AvalancheBulletin bulletin,
								  boolean grayscale, SimpleBindings dangerBindings, Path outputDirectory, boolean preview) throws IOException, MapyrusException, InterruptedException {

		final Path outputFile = outputDirectory.resolve(MapUtil.filename(region, mapLevel, daytimeDependency, bulletin, grayscale, MapImageFormat.pdf));
		final SimpleBindings bindings = new SimpleBindings(new TreeMap<>());
		bindings.put("xmax", region.getMapXmax());
		bindings.put("xmin", region.getMapXmin());
		bindings.put("ymax", region.getMapYmax());
		bindings.put("ymin", region.getMapYmin());
		bindings.put("image_type", "pdf");
		bindings.put("mapFile", outputFile);
		bindings.put("pagesize_x", mapLevel.width);
		bindings.put("pagesize_y", mapLevel.width / aspectRatio(region));
		bindings.put("geodata_dir", Paths.get(serverInstance.getMapProductionUrl()).resolve(region.getGeoDataDirectory()) + "/");
		bindings.put("map_level", mapLevel.name());
		MapProductionConfiguration config;
		switch (mapLevel) {
			case thumbnail:
				config = region.getThumbnailMapConfig();
				break;
			case overlay:
				config = region.getOverlayMapConfig();
				break;
			case standard:
			default:
				config = region.getStandardMapConfig();
				break;
		}
		bindings.put("rasterFile", config.getRasterFilePath());
		bindings.put("countriesShapeFile", config.getCountriesShapeFilePath());
		bindings.put("provincesShapeFile", config.getProvincesShapeFilePath());
		bindings.put("microRegionsShapeFile", config.getMicroRegionsShapeFilePath());
		bindings.put("riversShapeFile", config.getRiversShapeFilePath());
		bindings.put("lakesShapeFile", config.getLakesShapeFilePath());
		bindings.put("citiesShapeFile", config.getCitiesShapeFilePath());
		bindings.put("peaksShapeFile", config.getPeaksShapeFilePath());
		bindings.put("namesPShapeFile", config.getNamesPShapeFilePath());
		bindings.put("namesLShapeFile", config.getNamesLShapeFilePath());
		bindings.put("regionShapeFile", config.getRegionShapeFilePath());
		bindings.put("ppShapeFile", config.getPpShapeFilePath());

		bindings.put("colormode", grayscale ? "bw" : "col");
		bindings.put("dynamic_region", bulletin != null ? "one" : "all");
		bindings.put("scalebar",  MapLevel.overlay.equals(mapLevel) ? "off" : "on");
		bindings.put("copyright", MapLevel.overlay.equals(mapLevel) ? "off" : "on");
		bindings.put("logo_file", grayscale ? region.getMapLogoBwPath() : region.getMapLogoColorPath());
		bindings.put("logo_position", region.getLogoPosition().toString());
		bindings.put("bulletin_id", bulletin != null ? bulletin.getId() : region.getId());
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
		if (MapLevel.overlay.equals(mapLevel)) {
			mapyrus.interpret(Resources.getResource("mapyrus/albina_overlaymap.mapyrus"));
		} else {
			if (bulletin != null) {
				AvalancheBulletinDaytimeDescription description = daytimeDependency.getBulletinDaytimeDescription(bulletin);
				mapyrus.context.getBindings().put("elevation_level", description.getElevation());
				mapyrus.context.getBindings().put("danger_rating_low", DangerRating.getString(description.dangerRating(false)));
				mapyrus.context.getBindings().put("danger_rating_high", DangerRating.getString(description.dangerRating(true)));
			}
			mapyrus.interpret(Resources.getResource("mapyrus/albina_drawmap.mapyrus"));
		}

		final Path outputFilePng = MapImageFormat.png.convertFrom(outputFile);
		if (MapLevel.overlay.equals(mapLevel)) {
			MapImageFormat.pngTransparent.convertFrom(outputFilePng);
		}

		MapImageFormat.jpg.convertFrom(outputFilePng);
		if (DaytimeDependency.pm.equals(daytimeDependency) && bulletin == null) {
			// create combined am/pm maps
			final String amFile = outputDirectory.resolve(MapUtil.filename(region, mapLevel, DaytimeDependency.am, null, grayscale, MapImageFormat.jpg)).toString();
			final String pmFile = outputDirectory.resolve(MapUtil.filename(region, mapLevel, DaytimeDependency.pm, null, grayscale, MapImageFormat.jpg)).toString();
			final String fdFile = outputDirectory.resolve(MapUtil.filename(region, mapLevel, DaytimeDependency.fd, null, grayscale, MapImageFormat.jpg)).toString();
			logger.info("Combining {} and {} to {}", amFile, pmFile, fdFile);
			new ProcessBuilder("convert", "+append", amFile, pmFile, fdFile).inheritIO().start().waitFor();
		}

		if (!preview) {
			MapImageFormat.webp.convertFrom(outputFilePng);
		}
	}

	static double aspectRatio(Region region) {
		return ((double) region.getMapXmax() - (double) region.getMapXmin()) / ((double) region.getMapYmax() - (double) region.getMapYmin());
	}

	static String filename(Region region, MapLevel mapLevel) {
		return region.getId() + "_" + mapLevel.toString();
	}

	static String filename(Region region, MapLevel mapLevel, DaytimeDependency daytimeDependency, AvalancheBulletin bulletin, boolean grayscale, MapImageFormat format) {
		StringBuilder sb = new StringBuilder();
		if (bulletin == null) {
			sb.append(daytimeDependency.name());
			sb.append("_");
			sb.append(MapUtil.filename(region, mapLevel));
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
}
