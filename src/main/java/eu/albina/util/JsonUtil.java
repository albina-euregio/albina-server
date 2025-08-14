// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.openjson.JSONArray;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonUtil {

	// private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

	public static final ObjectMapper ALBINA_OBJECT_MAPPER = new ObjectMapper()
		.registerModule(new JavaTimeModule())
		.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
		.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public static void createJsonFile(AvalancheReport avalancheReport) throws IOException {
		Path pdfDirectory = avalancheReport.getPdfDirectory();
		Files.createDirectories(pdfDirectory);
		Path path = pdfDirectory.resolve(avalancheReport.getRegion().getId() + ".json");

		if (!avalancheReport.getBulletins().isEmpty()) {
			JSONArray jsonArray = JsonUtil.createJSONString(avalancheReport.getBulletins(), avalancheReport.getRegion(), true);
			String jsonString = jsonArray.toString();

			Files.writeString(path, jsonString, StandardCharsets.UTF_8);
		}
	}

	public static JSONArray createJSONString(Collection<AvalancheBulletin> bulletins, Region region, boolean small) {
		JSONArray jsonResult = new JSONArray();
		if (bulletins != null) {
			AvalancheBulletin b;
			for (AvalancheBulletin bulletin : bulletins) {
				b = new AvalancheBulletin();
				b.copy(bulletin);
				b.setId(bulletin.getId());

				// delete all published regions which are foreign
				if (b.getPublishedRegions() != null) {
					Set<String> newPublishedRegions = b.getPublishedRegions().stream()
						.filter(publishedRegion -> region.affects(publishedRegion))
						.collect(Collectors.toSet());
					b.setPublishedRegions(newPublishedRegions);
				}

				// delete all saved regions which are foreign
				if (b.getSavedRegions() != null) {
					Set<String> newSavedRegions = b.getSavedRegions().stream()
						.filter(savedRegion -> region.affects(savedRegion))
						.collect(Collectors.toSet());
					b.setSavedRegions(newSavedRegions);
				}

				// delete all suggested regions which are foreign
				if (b.getSuggestedRegions() != null) {
					Set<String> newSuggestedRegions = b.getSuggestedRegions().stream()
						.filter(suggestedRegion -> region.affects(suggestedRegion))
						.collect(Collectors.toSet());
					b.setSuggestedRegions(newSuggestedRegions);
				}

				if (small)
					jsonResult.put(b.toSmallJSON());
				else
					jsonResult.put(b.toJSON());
			}
		}
		return jsonResult;
	}

	public static <T> T parseUsingJackson(String json, Class<T> valueType) {
		try {
			return ALBINA_OBJECT_MAPPER.readValue(json, valueType);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String writeValueUsingJackson(Object value) {
		try {
			return ALBINA_OBJECT_MAPPER.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T parseUsingJackson(InputStream json, Class<T> valueType) throws IOException {
		try {
			return ALBINA_OBJECT_MAPPER.readValue(json, valueType);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeValueUsingJackson(OutputStream out, Object value) throws IOException {
		try {
			ALBINA_OBJECT_MAPPER.writeValue(out, value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
