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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.script.SimpleBindings;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.common.io.Resources;

import eu.albina.model.AvalancheReport;
import eu.albina.model.enumerations.BulletinStatus;
import org.mapyrus.Argument;
import org.mapyrus.FileOrURL;
import org.mapyrus.MapyrusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.ServerInstance;
import eu.albina.model.Region;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.DaytimeDependency;

public interface MapUtil {

	Logger logger = LoggerFactory.getLogger(MapUtil.class);

	static String getOverviewMapFilename(Region region, DaytimeDependency daytimeDependency,
			boolean grayscale) {
		return filename(region, MapLevel.standard, daytimeDependency, grayscale, MapImageFormat.jpg);
	}

	static SimpleBindings createMayrusBindings(List<AvalancheBulletin> bulletins, DaytimeDependency daytimeDependency, boolean preview) {
		Table<String, String, Argument> bindings = TreeBasedTable.create();
		for (AvalancheBulletin bulletin : bulletins) {
			Stream<String> regions = bulletin.getValidityDate().isBefore(ZonedDateTime.parse("2022-10-01T00:00:00Z"))
				? bulletin.regions(preview).stream().flatMap(MapUtil::mapRegions)
				: bulletin.regions(preview).stream();
			regions.forEach(region -> {
				AvalancheBulletinDaytimeDescription description = daytimeDependency.getBulletinDaytimeDescription(bulletin);
				bindings.put("bulletin_ids", region + "-h", new Argument(Argument.STRING, bulletin.getId()));
				bindings.put("bulletin_ids", region + "-l", new Argument(Argument.STRING, bulletin.getId()));
				bindings.put("danger_h", region + "-h", new Argument(DangerRating.getInt(description.dangerRating(true))));
				bindings.put("danger_l", region + "-l", new Argument(DangerRating.getInt(description.dangerRating(false))));
				bindings.put("elevation_h", region + "-h", new Argument(description.getElevation()));
			});
		}
		SimpleBindings simpleBindings = new SimpleBindings();
		bindings.rowMap().forEach((key, stringObjectMap) -> {
			Argument argument = new Argument();
			stringObjectMap.forEach(argument::addHashMapEntry);
			simpleBindings.put(key, argument);
		});
		return simpleBindings;
	}

	static Stream<String> mapRegions(String region) {
		switch (region) {
			case "AT-07-02": return Stream.of("AT-07-02-01", "AT-07-02-02");
			case "AT-07-04": return Stream.of("AT-07-04-01", "AT-07-04-02");
			case "AT-07-14": return Stream.of("AT-07-14-01", "AT-07-14-02", "AT-07-14-03", "AT-07-14-04", "AT-07-14-05");
			case "AT-07-17": return Stream.of("AT-07-17-01", "AT-07-17-02");
			case "IT-32-BZ-01": return Stream.of("IT-32-BZ-01-01", "IT-32-BZ-01-02");
			case "IT-32-BZ-02": return Stream.of("IT-32-BZ-02-01", "IT-32-BZ-02-02");
			case "IT-32-BZ-04": return Stream.of("IT-32-BZ-04-01", "IT-32-BZ-04-02");
			case "IT-32-BZ-05": return Stream.of("IT-32-BZ-05-01", "IT-32-BZ-05-02", "IT-32-BZ-05-03");
			case "IT-32-BZ-07": return Stream.of("IT-32-BZ-07-01", "IT-32-BZ-07-02");
			case "IT-32-BZ-08": return Stream.of("IT-32-BZ-08-01", "IT-32-BZ-08-02", "IT-32-BZ-08-03");
			case "IT-32-BZ-18": return Stream.of("IT-32-BZ-18-01", "IT-32-BZ-18-02");
			default: return Stream.of(region);
		}
	}

	static void createMapyrusMaps(AvalancheReport avalancheReport) {
		try {
			logger.info("Creating directory {}", avalancheReport.getMapsPath());
			Files.createDirectories(avalancheReport.getMapsPath());
		} catch (IOException ex) {
			throw new AlbinaMapException("Failed to create output directory", ex);
		}

		for (DaytimeDependency daytimeDependency : DaytimeDependency.of(avalancheReport.getBulletins())) {
			try {
				final SimpleBindings bindings = createMayrusBindings(avalancheReport.getBulletins(), daytimeDependency, avalancheReport.getStatus() == BulletinStatus.draft);
				for (MapLevel mapLevel : MapLevel.values()) {
					createMapyrusMaps(avalancheReport, mapLevel, daytimeDependency, null, false, bindings);
					createMapyrusMaps(avalancheReport, mapLevel, daytimeDependency, null, true, bindings);
				}
				for (final AvalancheBulletin bulletin : avalancheReport.getBulletins()) {
					if (DaytimeDependency.pm.equals(daytimeDependency) && !bulletin.isHasDaytimeDependency()) {
						continue;
					}
					if (!(avalancheReport.getStatus() == BulletinStatus.draft) && !bulletin.affectsRegionOnlyPublished(avalancheReport.getRegion())) {
						continue;
					}
					if (avalancheReport.getStatus() == BulletinStatus.draft && !bulletin.affectsRegionWithoutSuggestions(avalancheReport.getRegion())) {
						continue;
					}
					createMapyrusMaps(avalancheReport, MapLevel.thumbnail, daytimeDependency, bulletin, false, bindings);
					createMapyrusMaps(avalancheReport, MapLevel.thumbnail, daytimeDependency, bulletin, true, bindings);
				}
			} catch (IOException | MapyrusException | InterruptedException ex) {
				throw new AlbinaMapException("Failed to create mapyrus maps", ex);
			}
		}
	}

	static void createMapyrusMaps(AvalancheReport avalancheReport, MapLevel mapLevel, DaytimeDependency daytimeDependency, AvalancheBulletin bulletin,
								  boolean grayscale, SimpleBindings dangerBindings) throws IOException, MapyrusException, InterruptedException {
		final Region region = avalancheReport.getRegion();
		final ServerInstance serverInstance = avalancheReport.getServerInstance();
		final boolean preview = avalancheReport.getStatus() == BulletinStatus.draft;
		final Path outputDirectory = avalancheReport.getMapsPath();
		final Path outputFile = outputDirectory.resolve(bulletin == null
			? filename(region, mapLevel, daytimeDependency, grayscale, MapImageFormat.pdf)
			: filename(region, bulletin, daytimeDependency, grayscale, MapImageFormat.pdf));
		String logoPath = "";
		double logoAspectRatio = 1;

		if (grayscale && region.getMapLogoBwPath() != null && !region.getMapLogoBwPath().isEmpty()) {
			URL logoUrl = Resources.getResource(region.getMapLogoBwPath());
			logoPath = logoUrl.toString();
			BufferedImage image = ImageIO.read(logoUrl);
   			logoAspectRatio = (double) image.getWidth() / (double) image.getHeight();
		} else if (!grayscale && region.getMapLogoBwPath() != null && !region.getMapLogoBwPath().isEmpty()) {
			URL logoUrl = Resources.getResource(region.getMapLogoColorPath());
			logoPath = logoUrl.toString();
			BufferedImage image = ImageIO.read(logoUrl);
   			logoAspectRatio = (double) image.getWidth() / (double) image.getHeight();
		}

		final SimpleBindings bindings = new SimpleBindings(new TreeMap<>());
		final Path geodataPath = Paths.get(serverInstance.getMapProductionUrl()).resolve(region.getGeoDataDirectory());

		bindings.put("xmax", region.getMapXmax());
		bindings.put("xmin", region.getMapXmin());
		bindings.put("ymax", region.getMapYmax());
		bindings.put("ymin", region.getMapYmin());
		bindings.put("image_type", "pdf");
		bindings.put("mapFile", outputFile);
		bindings.put("pagesize_x", mapLevel.width);
		bindings.put("pagesize_y", mapLevel.width / aspectRatio(region));
		bindings.put("map_level", mapLevel.name());

		bindings.put("raster", MapUtil.mapProductionResource(geodataPath, "raster.png"));
		bindings.put("cities_p", MapUtil.mapProductionResource(geodataPath, "cities_p.shp"));
		bindings.put("labels_p", MapUtil.mapProductionResource(geodataPath, "labels_p.shp"));
		bindings.put("labels_l", MapUtil.mapProductionResource(geodataPath, "labels_l.shp"));
		bindings.put("passe_partout", MapUtil.mapProductionResource(geodataPath, "passe_partout.shp"));
		switch (mapLevel) {
			case thumbnail:
				bindings.put("countries_l", MapUtil.mapProductionResource(geodataPath, "countries_l_simplified.shp"));
				bindings.put("provinces_l", MapUtil.mapProductionResource(geodataPath, "provinces_l_simplified.shp"));
				bindings.put("micro_regions_elevation_a", MapUtil.mapProductionResource(geodataPath, "micro_regions_elevation_a_simplified.shp"));
				bindings.put("rivers_l", MapUtil.mapProductionResource(geodataPath, "rivers_l_simplified.shp"));
				bindings.put("lakes_a", MapUtil.mapProductionResource(geodataPath, "lakes_a_simplified.shp"));
				bindings.put("region_a", MapUtil.mapProductionResource(geodataPath, "region_a_simplified.shp"));
				break;
			case overlay:
			case standard:
			default:
				bindings.put("countries_l", MapUtil.mapProductionResource(geodataPath, "countries_l.shp"));
				bindings.put("provinces_l", MapUtil.mapProductionResource(geodataPath, "provinces_l.shp"));
				bindings.put("micro_regions_elevation_a", MapUtil.mapProductionResource(geodataPath, "micro_regions_elevation_a.shp"));
				bindings.put("rivers_l", MapUtil.mapProductionResource(geodataPath, "rivers_l.shp"));
				bindings.put("lakes_a", MapUtil.mapProductionResource(geodataPath, "lakes_a.shp"));
				bindings.put("regionShapeFile", MapUtil.mapProductionResource(geodataPath, "region.shp"));
				bindings.put("region_a", MapUtil.mapProductionResource(geodataPath, "region_a.shp"));
				break;
		}

		bindings.put("colormode", grayscale ? "bw" : "col");
		bindings.put("dynamic_region", bulletin != null ? "one" : "all");
		bindings.put("scalebar",  MapLevel.overlay.equals(mapLevel) ? "off" : "on");
		bindings.put("copyright", MapLevel.overlay.equals(mapLevel) ? "off" : "on");
		bindings.put("logo_file", logoPath);
		bindings.put("logo_position", region.getMapLogoPosition().toString());
		bindings.put("logo_aspect_ratio", logoAspectRatio);
		bindings.put("bulletin_id", bulletin != null ? bulletin.getId() : region.getId());
		bindings.putAll(dangerBindings);

		final String otf_mapyrus = String.format("let otf_mapyrus = \" otffiles=%s,%s,%s,%s,%s,%s \"",
			Resources.getResource("fonts/open-sans/OpenSans.otf").getFile(),
			Resources.getResource("fonts/open-sans/OpenSans-Italic.otf").getFile(),
			Resources.getResource("fonts/open-sans/OpenSans-Bold.otf").getFile(),
			Resources.getResource("fonts/open-sans/OpenSans-BoldItalic.otf").getFile(),
			Resources.getResource("fonts/open-sans/OpenSans-Semibold.otf").getFile(),
			Resources.getResource("fonts/open-sans/OpenSans-SemiboldItalic.otf").getFile());

		logger.debug("Creating map {} using {} with bindings {}", outputFile, dangerBindings, bindings);
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
			final String amFile = outputDirectory.resolve(filename(region, mapLevel, DaytimeDependency.am, grayscale, MapImageFormat.jpg)).toString();
			final String pmFile = outputDirectory.resolve(filename(region, mapLevel, DaytimeDependency.pm, grayscale, MapImageFormat.jpg)).toString();
			final String fdFile = outputDirectory.resolve(filename(region, mapLevel, DaytimeDependency.fd, grayscale, MapImageFormat.jpg)).toString();
			logger.debug("Combining {} and {} to {}", amFile, pmFile, fdFile);
			new ProcessBuilder("convert", "+append", amFile, pmFile, fdFile).inheritIO().start().waitFor();
		}

		if (!preview) {
			MapImageFormat.webp.convertFrom(outputFilePng);
		}
	}

	static Path mapProductionResource(Path geodataPath, String filename) {
		Path path = geodataPath.resolve(filename);
		if (Files.exists(path)) {
			return path;
		} else {
			return geodataPath.subpath(0, geodataPath.getNameCount() - 1).resolve(filename);
		}
	}

	static double aspectRatio(Region region) {
		return ((double) region.getMapXmax() - (double) region.getMapXmin()) / ((double) region.getMapYmax() - (double) region.getMapYmin());
	}

	static String filename(Region region, MapLevel mapLevel, DaytimeDependency daytimeDependency, boolean grayscale, MapImageFormat format) {
		return daytimeDependency.name() +
			"_" +
			region.getId() +
			"_" +
			mapLevel.toString() +
			(grayscale ? "_bw" : "") +
			"." +
			format;
	}

	static String filename(Region region, AvalancheBulletin bulletin, DaytimeDependency daytimeDependency, boolean grayscale, MapImageFormat format) {
		return region.getId() +
			"_" +
			bulletin.getId() +
			(DaytimeDependency.pm.equals(daytimeDependency) ? "_PM" : "") +
			(grayscale ? "_bw" : "") +
			"." +
			format;
	}
}
