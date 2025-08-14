// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.caaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
			Path path = dirPath.resolve(avalancheReport.getValidityDateString() + "_" + avalancheReport.getRegion().getId() + "_" + lang.toString() + version.filenameSuffix());
			Files.writeString(path, caamlString, StandardCharsets.UTF_8);
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
