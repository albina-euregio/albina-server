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
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
				Set<String> newPublishedRegions = new HashSet<String>();
				if (b.getPublishedRegions() != null) {
					for (String publishedRegion : b.getPublishedRegions()) {
						if (publishedRegion.startsWith(region.getId()))
							newPublishedRegions.add(publishedRegion);
					}
					b.setPublishedRegions(newPublishedRegions);
				}

				// delete all saved regions which are foreign
				Set<String> newSavedRegions = new HashSet<String>();
				if (b.getSavedRegions() != null) {
					for (String savedRegion : b.getSavedRegions()) {
						if (savedRegion.startsWith(region.getId()))
							newSavedRegions.add(savedRegion);
					}
					b.setSavedRegions(newSavedRegions);
				}

				// delete all suggested regions which are foreign
				Set<String> newSuggestedRegions = new HashSet<String>();
				if (b.getSuggestedRegions() != null) {
					for (String suggestedRegion : b.getSuggestedRegions()) {
						if (suggestedRegion.startsWith(region.getId()))
							newSuggestedRegions.add(suggestedRegion);
					}
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
