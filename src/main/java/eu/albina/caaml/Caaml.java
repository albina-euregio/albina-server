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
package eu.albina.caaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import eu.albina.model.AvalancheReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.enumerations.LanguageCode;

/**
 * CAAML (Canadian Avalanche Association Markup Language) is a standard for the electronic representation
 * of information pertinent to avalanche safety operations.
 *
 * @see <a href="http://caaml.org/">caaml.org</a>
 * @see <a href="https://gitlab.com/albina-euregio/albina-caaml">gitlab.com/albina-euregio/albina-caaml</a>
 */
public interface Caaml {

	Logger logger = LoggerFactory.getLogger(Caaml.class);

	static void createCaamlFiles(AvalancheReport avalancheReport, CaamlVersion version) throws IOException {
		Path dirPath = avalancheReport.getPdfDirectory();
		Files.createDirectories(dirPath);

		for (LanguageCode lang : avalancheReport.getRegion().getEnabledLanguages()) {
			String caamlString = createCaaml(avalancheReport, lang, version);
			String fileName = dirPath + "/" + avalancheReport.getValidityDateString() + "_" + avalancheReport.getRegion().getId() + "_" + lang.toString() + version.filenameSuffix();
			Files.write(Paths.get(fileName), caamlString.getBytes(StandardCharsets.UTF_8));
		}
	}

	static String createCaaml(AvalancheReport avalancheReport, LanguageCode lang, CaamlVersion version) {
		if (version == CaamlVersion.V5) {
			return Caaml5.createCaamlv5(avalancheReport, lang);
		} else if (version == CaamlVersion.V6_JSON) {
			return Caaml6.createJSON(avalancheReport, lang);
		} else {
			return Caaml6.createXML(avalancheReport, lang);
		}
	}

}
