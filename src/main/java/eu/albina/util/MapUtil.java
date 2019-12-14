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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.script.SimpleBindings;
import javax.xml.transform.TransformerException;

import org.mapyrus.ContextStack;
import org.mapyrus.FileOrURL;
import org.mapyrus.Interpreter;
import org.mapyrus.MapyrusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

public class MapUtil {

	private static final Logger logger = LoggerFactory.getLogger(MapUtil.class);
	private static final String AVALANCHE_WARNING_MAPS_PATH = "/home/simon/src/albina-euregio/avalanche-warning-maps/";

	// REGION
	static String getOverviewMapFilename(String region, boolean isAfternoon, boolean hasDaytimeDependency,
										 boolean grayscale) {
		return Map.forRegion(region)
				.orElse(Map.fullmap)
				.filename(isAfternoon, hasDaytimeDependency, grayscale, "jpg");
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

		// TODO implement local creation of danger rating maps
		// TODO delete copying of maps

		try {
			Document doc = XmlUtil.createCaaml(bulletins, LanguageCode.en);
			triggerMapProductionUnivie(XmlUtil.convertDocToString(doc));
		} catch (AlbinaException | TransformerException e) {
			logger.error("Error producing maps: " + e.getMessage());
			e.printStackTrace();
		}
	}

	static String createMayrusInput(List<AvalancheBulletin> bulletins) {
		final String header = "sys_bid;bid;region;date;am_pm;validelevation;dr_h;dr_l;aspect_h;aspect_l;avprob_h;avprob_l\n";
		return header + bulletins.stream()
				.flatMap(bulletin -> bulletin.getPublishedRegions().stream()
						.map(region -> String.join(";",
								Integer.toString(bulletins.indexOf(bulletin)),
								bulletin.getId(),
								region,
								bulletin.getValidityDateString(),
								"fd",
								Integer.toString(bulletin.getElevation()),
								DangerRating.getString(bulletin.getForenoon().getDangerRatingAbove()),
								DangerRating.getString(bulletin.isHasElevationDependency()
										? bulletin.getForenoon().getDangerRatingBelow()
										: bulletin.getForenoon().getDangerRatingAbove()),
								"0",
								"0",
								"0",
								"0"
						))
				)
				.collect(Collectors.joining("\n"));
	}

	enum Map {
		fullmap,
		overlay,
		tyrol(GlobalVariables.codeTyrol, 1452000, 1116000, 6053000, 5829000),
		southtyrol(GlobalVariables.codeSouthTyrol, 1400000, 1145000, 5939000, 5769000),
		trentino(GlobalVariables.codeTrentino, 1358000, 1133000, 5842000, 5692000),
		fullmap_small;

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

		String filename(boolean isAfternoon, boolean hasDaytimeDependency, boolean grayscale, String format) {
			StringBuilder sb = new StringBuilder();
			if (hasDaytimeDependency) {
				if (isAfternoon)
					sb.append("pm");
				else
					sb.append("am");
			} else
				sb.append("fd");
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

	static void createMapyrusMaps(List<AvalancheBulletin> bulletins) throws MapyrusException, IOException, InterruptedException {
		// TODO handle daytime dependency
		final String date = bulletins.get(0).getValidityDateString();
		final Path outputDirectory = Paths.get("/tmp/" + date + "/");
		Files.createDirectories(outputDirectory);
		final Path drmFile = outputDirectory.resolve("fd.txt");
		Files.write(drmFile, createMayrusInput(bulletins).getBytes(StandardCharsets.UTF_8));
		for (Map map : Map.values()) {
			createMapyrusMaps(map, null, 0, false, date, drmFile);
			createMapyrusMaps(map, null, 0, true, date, drmFile);
		}
		for (int i = 0; i < bulletins.size(); i++) {
			createMapyrusMaps(Map.fullmap_small, bulletins.get(i).getId(), i, false, date, drmFile);
			createMapyrusMaps(Map.fullmap_small, bulletins.get(i).getId(), i, true, date, drmFile);
		}
	}

	static void createMapyrusMaps(Map map, String bulletinId, int bulletinIndex, boolean grayscale, String date, Path dangerRatingMapFile) throws IOException, MapyrusException, InterruptedException {
		final MapSize size = Map.overlay.equals(map)
				? MapSize.overlay
				: Map.fullmap_small.equals(map)
				? MapSize.thumbnail_map
				: MapSize.standard_map;
		final String mapyrusFile = Map.overlay.equals(map) ? "mapyrus/albina_overlaymap.mapyrus" : "mapyrus/albina_drawmap.mapyrus";
		final Interpreter mapyrus = new Interpreter();
		final ContextStack context = new ContextStack();
		final Path outputDirectory = dangerRatingMapFile.getParent();
		final Path outputFile = outputDirectory.resolve(bulletinId != null
				? bulletinId + (grayscale ? "_bw.pdf" : ".pdf")
				: map.filename(false, false, grayscale, "pdf"));
		context.setBindings(new SimpleBindings(new HashMap<String, Object>() {{
			put("xmax", map.xmax);
			put("xmin", map.xmin);
			put("ymax", map.ymax);
			put("ymin", map.ymin);
			put("image_type", "pdf");
			put("drm_file", dangerRatingMapFile);
			put("mapFile", outputFile);
			put("date", date);
			put("pagesize_x", size.width);
			put("pagesize_y", size.width / map.aspectRatio());
			put("map_xsize", size.width);
			put("working_dir", outputDirectory);
			put("font_dir", AVALANCHE_WARNING_MAPS_PATH + "mapyrus/fonts/");
			put("geodata_dir", AVALANCHE_WARNING_MAPS_PATH + "geodata/");
			put("image_dir", AVALANCHE_WARNING_MAPS_PATH + "images/");
			put("region", "Euregio");
			put("level", size.ordinal() + 1);
			put("colormode", grayscale ? "bw" : "col");
			put("dynamic_region", bulletinId != null ? "one" : "all");
			put("language", "en");
			put("scalebar", Map.overlay.equals(map) ? "off" : "on");
			put("copyright", Map.overlay.equals(map) ? "off" : "on");
			put("interreg", Map.fullmap.equals(map) ? "on" : "off");
			put("bulletin_id", bulletinId != null ? bulletinIndex : map.name());
		}}));
		final FileOrURL file = new FileOrURL(AVALANCHE_WARNING_MAPS_PATH + mapyrusFile);
		mapyrus.interpret(context, file, System.in, System.out);

		final int dpi = 300;
		new ProcessBuilder("gs", "-sDEVICE=png16m", "-dTextAlphaBits=4", "-dGraphicsAlphaBits=4", "-r" + dpi, "-o",
				outputFile.toString().replaceFirst("pdf$", "png"),
				outputFile.toString()
		).inheritIO().start().waitFor();
		new ProcessBuilder("cwebp",
				outputFile.toString().replaceFirst("pdf$", "png"), "-o",
				outputFile.toString().replaceFirst("pdf$", "webp")
		).inheritIO().start().waitFor();
		new ProcessBuilder("gs", "-sDEVICE=jpeg", "-dJPEGQ=80", "-dTextAlphaBits=4", "-dGraphicsAlphaBits=4", "-r" + dpi, "-o",
				outputFile.toString().replaceFirst("pdf$", "jpg"),
				outputFile.toString()
		).inheritIO().start().waitFor();
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
			e.printStackTrace();
			throw new AlbinaException(e.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
