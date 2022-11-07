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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;

import com.github.openjson.JSONArray;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;

public class JsonUtil {

	// private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

	public static void createJsonFile(AvalancheReport avalancheReport) throws TransformerException, IOException {
		Path pdfDirectory = avalancheReport.getPdfDirectory();
		Files.createDirectories(pdfDirectory);
		Path path = pdfDirectory.resolve(avalancheReport.getRegion().getId() + ".json");

		if (!avalancheReport.getBulletins().isEmpty()) {
			JSONArray jsonArray = JsonUtil.createJSONString(avalancheReport.getBulletins(), avalancheReport.getRegion(), true);
			String jsonString = jsonArray.toString();

			Files.write(path, jsonString.getBytes(StandardCharsets.UTF_8));
			AlbinaUtil.setFilePermissions(path.toString());
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
						.filter(publishedRegion -> publishedRegion.startsWith(region.getId()))
						.collect(Collectors.toSet());
					b.setPublishedRegions(newPublishedRegions);
				}

				// delete all saved regions which are foreign
				if (b.getSavedRegions() != null) {
					Set<String> newSavedRegions = b.getSavedRegions().stream()
						.filter(savedRegion -> savedRegion.startsWith(region.getId()))
						.collect(Collectors.toSet());
					b.setSavedRegions(newSavedRegions);
				}

				// delete all suggested regions which are foreign
				if (b.getSuggestedRegions() != null) {
					Set<String> newSuggestedRegions = b.getSuggestedRegions().stream()
						.filter(suggestedRegion -> suggestedRegion.startsWith(region.getId()))
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
}
