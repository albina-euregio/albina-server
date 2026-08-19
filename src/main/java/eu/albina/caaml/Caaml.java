// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.caaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import eu.albina.model.AvalancheReport;

import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;

import eu.albina.model.enumerations.LanguageCode;
import jakarta.inject.Singleton;

/**
 * CAAML (Canadian Avalanche Association Markup Language) is a standard for the electronic representation
 * of information pertinent to avalanche safety operations.
 *
 * @see <a href="http://caaml.org/">caaml.org</a>
 * @see <a href="https://gitlab.com/albina-euregio/albina-caaml">gitlab.com/albina-euregio/albina-caaml</a>
 */
@Singleton
public class Caaml {

	@Inject
	private ObjectMapper objectMapper;

	public void createCaamlFiles(AvalancheReport avalancheReport) throws IOException {
		Path dirPath = avalancheReport.getPdfDirectory();
		Files.createDirectories(dirPath);

		for (LanguageCode lang : avalancheReport.getRegion().getEnabledLanguages()) {
			Caaml6 caaml6 = new Caaml6(avalancheReport, lang);
			Path pathJSON = dirPath.resolve("%s_%s_%s_CAAMLv6.json".formatted(avalancheReport.getValidityDateString(), avalancheReport.getRegion().getId(), lang));
			Files.writeString(pathJSON, caaml6.createJSON(objectMapper), StandardCharsets.UTF_8);
			Path pathXML = dirPath.resolve("%s_%s_%s_CAAMLv6.xml".formatted(avalancheReport.getValidityDateString(), avalancheReport.getRegion().getId(), lang));
			Files.writeString(pathXML, caaml6.createXML(), StandardCharsets.UTF_8);
		}
	}

	public String createCaaml(AvalancheReport avalancheReport, LanguageCode lang, CaamlVersion version) {
		Caaml6 caaml6 = new Caaml6(avalancheReport, lang);
		if (version == CaamlVersion.V6_JSON) {
			return caaml6.createJSON(objectMapper);
		} else {
			return caaml6.createXML();
		}
	}

}
