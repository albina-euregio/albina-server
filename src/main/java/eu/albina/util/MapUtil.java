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
package eu.albina.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.SimpleBindings;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTimeZone;
import org.mapyrus.ContextStack;
import org.mapyrus.FileOrURL;
import org.mapyrus.Interpreter;
import org.mapyrus.MapyrusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

import eu.albina.controller.RegionController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.Region;
import eu.albina.model.Regions;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

public class MapUtil {

	private static final Logger logger = LoggerFactory.getLogger(MapUtil.class);

	public static class AlbinaMapException extends RuntimeException {
		public AlbinaMapException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	// REGION
	static String getOverviewMapFilename(String region, boolean isAfternoon, boolean hasDaytimeDependency,
			boolean grayscale) {
		final DaytimeDependency daytimeDependency = !hasDaytimeDependency ? DaytimeDependency.fd
				: isAfternoon ? DaytimeDependency.pm : DaytimeDependency.am;
		return Map.forRegion(region).orElse(Map.fullmap).filename(daytimeDependency, grayscale, "jpg");
	}

	/**
	 * Create images of each map needed for the different products of
	 * avalanche.report. This consists of an overview map over the whole EUREGIO,
	 * maps for each province and detailed maps for each aggregated region.
	 *
	 * @param bulletins
	 *            The bulletins to create the maps from.
	 */
	public static void createDangerRatingMaps(List<AvalancheBulletin> bulletins) {
		final long start = System.currentTimeMillis();
		logger.info("Creating danger rating maps for {} using {}", AlbinaUtil.getValidityDateString(bulletins),
				GlobalVariables.getMapProductionUrl());
		if (GlobalVariables.isMapProductionUrlUnivie()) {
			createDangerRatingMapsUnivie(bulletins);
		} else {
			try {
				createMapyrusMaps(bulletins);
			} catch (Exception ex) {
				logger.error("Failed to create mapyrus maps", ex);
				throw ex;
			}
		}
		logger.info("Creating danger rating maps done in {} ms", System.currentTimeMillis() - start);
	}

	private static void createDangerRatingMapsUnivie(List<AvalancheBulletin> bulletins) {
		try {
			Document doc = XmlUtil.createCaamlv5(bulletins, LanguageCode.en);
			triggerMapProductionUnivie(XmlUtil.convertDocToString(doc));
		} catch (AlbinaException | TransformerException e) {
			logger.error("Error producing maps", e);
		}
	}

	static String createMayrusInput(List<AvalancheBulletin> bulletins, DaytimeDependency daytimeDependency) {
		final String header = "sys_bid;bid;region;date;am_pm;validelevation;dr_h;dr_l;aspect_h;aspect_l;avprob_h;avprob_l\n";
		return header + IntStream.range(0, bulletins.size()).boxed()
				.flatMap(index -> bulletins.get(index).getPublishedRegions().stream().map(region -> {
					final AvalancheBulletin bulletin = bulletins.get(index);
					final AvalancheBulletinDaytimeDescription description = getBulletinDaytimeDescription(bulletin,
							daytimeDependency);
					return String.join(";", Integer.toString(index), bulletin.getId(), region,
							bulletin.getValidityDateString(), "", Integer.toString(bulletin.getElevation()),
							getDangerRatingString(bulletin, description, true),
							getDangerRatingString(bulletin, description, false), "0", "0", "0", "0");
				})).collect(Collectors.joining("\n"));
	}

	private static AvalancheBulletinDaytimeDescription getBulletinDaytimeDescription(AvalancheBulletin bulletin,
			DaytimeDependency daytimeDependency) {
		return bulletin.isHasDaytimeDependency() && DaytimeDependency.pm.equals(daytimeDependency)
				? bulletin.getAfternoon()
				: bulletin.getForenoon();
	}

	private static String getDangerRatingString(AvalancheBulletin bulletin,
			AvalancheBulletinDaytimeDescription description, boolean above) {
		return DangerRating.getString(bulletin.isHasElevationDependency() && !above ? description.getDangerRatingBelow()
				: description.getDangerRatingAbove());
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

		String filename(DaytimeDependency daytimeDependency, boolean grayscale, String format) {
			StringBuilder sb = new StringBuilder();
			sb.append(daytimeDependency.name());
			sb.append("_");
			sb.append(this.filename());

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
	}

	enum DaytimeDependency {
		/**
		 * full day
		 */
		fd, am, pm
	}

	static void createMapyrusMaps(List<AvalancheBulletin> bulletins) {
		final Path outputDirectory = Paths.get(GlobalVariables.getMapsPath(),
				AlbinaUtil.getValidityDateString(bulletins), AlbinaUtil.getPublicationTime(bulletins));
		try {
			logger.info("Creating directory {}", outputDirectory);
			Files.createDirectories(outputDirectory);
		} catch (IOException ex) {
			throw new AlbinaMapException("Failed to create output directory", ex);
		}
		final boolean hasDaytimeDependency = bulletins.stream().anyMatch(AvalancheBulletin::isHasDaytimeDependency);
		final List<Callable<Object>> mapTasks = new ArrayList<>();
		for (DaytimeDependency daytimeDependency : hasDaytimeDependency
				? EnumSet.of(DaytimeDependency.am, DaytimeDependency.pm)
				: EnumSet.of(DaytimeDependency.fd)) {
			try {
				final Path regionFile = outputDirectory.resolve(daytimeDependency + "_regions.json");
				logger.info("Creating region file {}", regionFile);
				createBulletinRegions(bulletins, daytimeDependency, regionFile,
						RegionController.getInstance().getRegions());
			} catch (IOException | AlbinaException ex) {
				throw new AlbinaMapException("Failed to create region file", ex);
			}
			final Path drmFile;
			try {
				drmFile = outputDirectory.resolve(daytimeDependency + ".txt");
				logger.info("Creating mapyrus input file {}", drmFile);
				Files.write(drmFile, createMayrusInput(bulletins, daytimeDependency).getBytes(StandardCharsets.UTF_8));
			} catch (IOException ex) {
				throw new AlbinaMapException("Failed to create mapyrus input file", ex);
			}
			try {
				for (Map map : Map.values()) {
					createMapyrusMaps(map, daytimeDependency, null, 0, false, drmFile);
					createMapyrusMaps(map, daytimeDependency, null, 0, true, drmFile);
				}
				for (int i = 0; i < bulletins.size(); i++) {
					final AvalancheBulletin bulletin = bulletins.get(i);
					if (DaytimeDependency.pm.equals(daytimeDependency) && !bulletin.isHasDaytimeDependency()) {
						continue;
					}
					createMapyrusMaps(Map.fullmap_small, daytimeDependency, bulletin.getId(), i, false, drmFile);
					createMapyrusMaps(Map.fullmap_small, daytimeDependency, bulletin.getId(), i, true, drmFile);
				}
			} catch (IOException | MapyrusException | InterruptedException ex) {
				throw new AlbinaMapException("Failed to create mapyrus maps", ex);
			}
		}
	}

	static void createMapyrusMaps(Map map, DaytimeDependency daytimeDependency, String bulletinId, int bulletinIndex,
			boolean grayscale, Path dangerRatingMapFile) throws IOException, MapyrusException, InterruptedException {
		final MapSize size = Map.overlay.equals(map) ? MapSize.overlay
				: Map.fullmap_small.equals(map) ? MapSize.thumbnail_map : MapSize.standard_map;
		final String mapProductionUrl = GlobalVariables.getMapProductionUrl();
		final String mapyrusFile = Map.overlay.equals(map) ? "mapyrus/albina_overlaymap.mapyrus"
				: "mapyrus/albina_drawmap.mapyrus";
		final Interpreter mapyrus = new Interpreter();
		final ContextStack context = new ContextStack();
		final Path outputDirectory = dangerRatingMapFile.getParent();
		final Path outputFile = outputDirectory.resolve(
				bulletinId != null
						? bulletinId + (DaytimeDependency.pm.equals(daytimeDependency) ? "_PM" : "")
								+ (grayscale ? "_bw.pdf" : ".pdf")
						: map.filename(daytimeDependency, grayscale, "pdf"));
		final Path tempDirectory = Files.createTempDirectory("mapyrus");
		final TreeMap<String, Object> bindings = new TreeMap<String, Object>() {
			{
				put("xmax", map.xmax);
				put("xmin", map.xmin);
				put("ymax", map.ymax);
				put("ymin", map.ymin);
				put("image_type", "pdf");
				put("drm_file", dangerRatingMapFile);
				put("mapFile", outputFile);
				put("pagesize_x", size.width);
				put("pagesize_y", size.width / map.aspectRatio());
				put("map_xsize", size.width);
				put("working_dir", tempDirectory + "/");
				put("font_dir", mapProductionUrl + "mapyrus/fonts/");
				put("geodata_dir", mapProductionUrl + "geodata/");
				put("image_dir", mapProductionUrl + "images/");
				put("region", "Euregio");
				put("level", size.ordinal() + 1);
				put("colormode", grayscale ? "bw" : "col");
				put("dynamic_region", bulletinId != null ? "one" : "all");
				put("language", "en");
				put("scalebar", Map.overlay.equals(map) ? "off" : "on");
				put("copyright", Map.overlay.equals(map) ? "off" : "on");
				put("interreg", Map.fullmap.equals(map) ? "on" : "off");
				put("bulletin_id", bulletinId != null ? bulletinIndex : map.name());
			}
		};
		context.setBindings(new SimpleBindings(bindings));
		final FileOrURL file = new FileOrURL(mapProductionUrl + mapyrusFile);
		logger.info("Creating map {} using {} and {} with bindings {}", outputFile, dangerRatingMapFile, mapyrusFile,
				bindings);
		mapyrus.interpret(context, file, System.in, System.out);
		deleteDirectoryWithContents(tempDirectory);

		final int dpi = 300;
		final String outputFileJpg = outputFile.toString().replaceFirst("pdf$", "jpg");
		final String outputFilePng = outputFile.toString().replaceFirst("pdf$", "png");
		final String outputFileWebp = outputFile.toString().replaceFirst("pdf$", "webp");

		logger.info("Converting {} to {}}", outputFile, outputFilePng);
		new ProcessBuilder("gs", "-sDEVICE=png16m", "-dTextAlphaBits=4", "-dGraphicsAlphaBits=4", "-r" + dpi, "-o",
				outputFilePng, outputFile.toString()).inheritIO().start().waitFor();

		logger.info("Converting {} to {}}", outputFilePng, outputFileWebp);
		new ProcessBuilder("cwebp", outputFilePng, "-o", outputFileWebp).inheritIO().start().waitFor();
		logger.info("Converting {} to {}}", outputFile, outputFileJpg);
		new ProcessBuilder("gs", "-sDEVICE=jpeg", "-dJPEGQ=80", "-dTextAlphaBits=4", "-dGraphicsAlphaBits=4",
				"-r" + dpi, "-o", outputFileJpg, outputFile.toString()).inheritIO().start().waitFor();
	}

	private static void deleteDirectoryWithContents(Path directory) throws IOException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
			for (Path path : directoryStream) {
				Files.delete(path);
			}
		}
		Files.delete(directory);
	}

	static void createBulletinRegions(List<AvalancheBulletin> bulletins, DaytimeDependency daytimeDependency,
			Path regionFile, Regions regions) throws IOException, AlbinaException {
		final GeoJson.FeatureCollection featureCollection = new GeoJson.FeatureCollection();
		featureCollection.properties.put("creation_date", bulletins.get(0).getPublicationDate()
				.withZone(DateTimeZone.UTC).toString(GlobalVariables.formatterDateTime));
		featureCollection.properties.put("valid_date", bulletins.get(0).getValidityDateString());
		featureCollection.properties.put("valid_daytime", daytimeDependency.name());
		for (AvalancheBulletin bulletin : bulletins) {
			final GeoJson.Feature feature = new GeoJson.Feature();
			final AvalancheBulletinDaytimeDescription description = getBulletinDaytimeDescription(bulletin,
					daytimeDependency);
			feature.properties.put("bid", bulletin.getId());
			feature.properties.put("daytime", daytimeDependency.name());
			feature.properties.put("elevation", bulletin.getElevation());
			feature.properties.put("dl_hi", getDangerRatingString(bulletin, description, true));
			feature.properties.put("dl_lo", getDangerRatingString(bulletin, description, false));
			feature.properties.put("problem_1", getAvalancheSituationString(description.getAvalancheSituation1()));
			feature.properties.put("problem_2", getAvalancheSituationString(description.getAvalancheSituation2()));
			final double bufferDistance = 1e-4;
			feature.geometry = regions.getRegionsForBulletin(bulletin).map(Region::getPolygon)
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

	private static String getAvalancheSituationString(AvalancheSituation avalancheSituation) {
		return Optional.ofNullable(avalancheSituation).map(AvalancheSituation::getAvalancheSituation)
				.map(eu.albina.model.enumerations.AvalancheSituation::toCaamlv5String).orElse("false");
	}

	public static String triggerMapProductionUnivie(String caaml) throws AlbinaException {
		HttpURLConnection connection = null;

		try {
			// Create connection
			URL url = new URL(GlobalVariables.mapProductionUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length", Integer.toString(caaml.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(caaml);
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();

			if (connection.getResponseCode() != 200 && connection.getResponseCode() != 200)
				throw new AlbinaException("Error while triggering map production!");

			return response.toString();
		} catch (Exception e) {
			logger.warn("Failed to trigger map production", e);
			throw new AlbinaException(e.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
