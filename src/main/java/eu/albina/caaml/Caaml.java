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
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import eu.albina.model.AvalancheReport;
import eu.albina.util.AlbinaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.enumerations.LanguageCode;
import org.w3c.dom.Document;

/**
 * CAAML (Canadian Avalanche Association Markup Language) is a standard for the electronic representation
 * of information pertinent to avalanche safety operations.
 *
 * @see <a href="http://caaml.org/">caaml.org</a>
 * @see <a href="https://gitlab.com/albina-euregio/albina-caaml">gitlab.com/albina-euregio/albina-caaml</a>
 */
public interface Caaml {

	Logger logger = LoggerFactory.getLogger(Caaml.class);

	static void createCaamlFiles(AvalancheReport avalancheReport, CaamlVersion version) throws TransformerException, IOException {
		Path dirPath = avalancheReport.getPdfDirectory();
		Files.createDirectories(dirPath);

		// using PosixFilePermission to set file permissions 777
		Set<PosixFilePermission> perms = EnumSet.allOf(PosixFilePermission.class);
		try {
			Files.setPosixFilePermissions(dirPath.getParent(), perms);
			Files.setPosixFilePermissions(dirPath, perms);
		} catch (IOException | UnsupportedOperationException e) {
			logger.warn("File permissions could not be set!");
		}

		for (LanguageCode lang : LanguageCode.ENABLED) {
			String caamlString = createCaaml(avalancheReport, lang, version);
			String fileName = dirPath + "/" + avalancheReport.getValidityDateString() + "_" + avalancheReport.getRegion().getId() + "_" + lang.toString() + version.filenameSuffix();
			Files.write(Paths.get(fileName), caamlString.getBytes(StandardCharsets.UTF_8));
			AlbinaUtil.setFilePermissions(fileName);
		}
	}

	static String createCaaml(AvalancheReport avalancheReport, LanguageCode lang, CaamlVersion version) {
		if (version == CaamlVersion.V5) {
			return Caaml5.createCaamlv5(avalancheReport, lang);
		} else if (version == CaamlVersion.V6_2022) {
			return Caaml6_2022.toCAAMLv6String_2022(avalancheReport, lang);
		} else {
			return Caaml6.createCaamlv6(avalancheReport, lang);
		}
	}

	static String convertDocToString(Document doc) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		final StringWriter stringWriter = new StringWriter();
		StreamResult result = new StreamResult(stringWriter);
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		return stringWriter.toString();
	}
}
