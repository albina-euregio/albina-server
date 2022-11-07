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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import eu.albina.model.AvalancheReport;
import eu.albina.map.MapImageFormat;
import eu.albina.map.MapLevel;
import eu.albina.map.MapUtil;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.LinkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;

public class Caaml {

	private static final Logger logger = LoggerFactory.getLogger(Caaml.class);

	public static void createCaamlFiles(AvalancheReport avalancheReport, CaamlVersion version) throws TransformerException, IOException {
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
			String fileName = dirPath + "/" + avalancheReport.getValidityDateString() + "_" + avalancheReport.getRegion().getId() + "_" + lang.toString();
			if (version == CaamlVersion.V5)
				fileName += ".xml";
			else if (version == CaamlVersion.V6_2022)
				fileName += "_CAAMLv6_2022.json";
			else
				fileName += "_CAAMLv6.xml";
			Files.write(Paths.get(fileName), caamlString.getBytes(StandardCharsets.UTF_8));
			AlbinaUtil.setFilePermissions(fileName);
		}
	}

	public static String createCaaml(AvalancheReport avalancheReport, LanguageCode lang, CaamlVersion version) {
		if (version == CaamlVersion.V5) {
			return Caaml.createCaamlv5(avalancheReport, lang);
		} else if (version == CaamlVersion.V6_2022) {
			return avalancheReport.toCAAMLv6String_2022(lang);
		} else {
			return Caaml.createCaamlv6(avalancheReport, lang);
		}
	}

	public static String createCaamlv5(AvalancheReport avalancheReport, LanguageCode language) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = CaamlVersion.V5.setNamespaceAttributes(doc.createElement("ObsCollection"));

			// create meta data
			List<AvalancheBulletin> bulletins = avalancheReport.getBulletins();
			if (bulletins != null && !bulletins.isEmpty()) {
				ZonedDateTime publicationDate = AlbinaUtil.getPublicationDate(bulletins);

				// metaData
				Element metaDataProperty = createMetaDataProperty(doc, publicationDate, language);
				rootElement.appendChild(metaDataProperty);

				// observations
				Element observations = doc.createElement("observations");

				for (AvalancheBulletin bulletin : bulletins) {
					List<Element> caaml = bulletin.toCAAMLv5(doc, language, avalancheReport.getRegion());
					if (caaml != null)
						for (Element element : caaml) {
							if (element != null)
								observations.appendChild(element);
						}
				}
				rootElement.appendChild(observations);

				// attributes
				if (language == null)
					language = LanguageCode.en;
				rootElement.setAttribute("xml:lang", language.toString());
			}

			doc.appendChild(rootElement);

			return convertDocToString(doc);
		} catch (ParserConfigurationException | TransformerException e1) {
			logger.error("Error producing CAAML", e1);
			return null;
		}
	}

	public static String createCaamlv6(AvalancheReport avalancheReport, LanguageCode language) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = CaamlVersion.V6.setNamespaceAttributes(doc.createElement("bulletins"));

			// create meta data
			List<AvalancheBulletin> bulletins = avalancheReport.getBulletins();
			if (bulletins != null && !bulletins.isEmpty()) {

				// metaData
				Element metaData = doc.createElement("metaData");
				for (Element extFile : createObsCollectionExtFiles(doc, bulletins, language, avalancheReport.getRegion(), avalancheReport.getServerInstance())) {
					metaData.appendChild(extFile);
				}
				rootElement.appendChild(metaData);

				String reportPublicationTime = AlbinaUtil.getPublicationTime(bulletins);

				for (AvalancheBulletin bulletin : bulletins) {
					List<Element> caaml = bulletin.toCAAMLv6(doc, language, avalancheReport.getRegion(), reportPublicationTime, avalancheReport.getServerInstance());
					if (caaml != null)
						for (Element element : caaml) {
							if (element != null)
								rootElement.appendChild(element);
						}
				}
			}

			doc.appendChild(rootElement);

			return convertDocToString(doc);
		} catch (ParserConfigurationException | TransformerException e1) {
			logger.error("Error producing CAAMLv6", e1);
			return null;
		}
	}

	public static String createValidElevationAttribute(int elevation, boolean above, boolean treeline) {
		if (treeline) {
			if (above)
				return "ElevationRange_TreelineHi";
			else
				return "ElevationRange_TreelineLw";
		} else {
			if (above)
				return "ElevationRange_" + elevation + "Hi";
			else
				return "ElevationRange_" + elevation + "Lw";
		}
	}

	public static Element createMetaDataProperty(Document doc, ZonedDateTime dateTime, LanguageCode language) {
		Element metaDataProperty = doc.createElement("metaDataProperty");
		Element metaData = doc.createElement("MetaData");
		Element dateTimeReport = doc.createElement("dateTimeReport");
		if (dateTime != null) {
			dateTimeReport.appendChild(doc
					.createTextNode(DateTimeFormatter.ISO_INSTANT.format(dateTime)));
			metaData.appendChild(dateTimeReport);
		}
		Element srcRef = doc.createElement("srcRef");
		Element operation = doc.createElement("Operation");
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(language.getBundleString("website.name")));
		operation.appendChild(name);
		srcRef.appendChild(operation);
		metaData.appendChild(srcRef);

		metaDataProperty.appendChild(metaData);
		return metaDataProperty;
	}

	private static List<Element> createObsCollectionExtFiles(Document doc, List<AvalancheBulletin> bulletins, LanguageCode lang, Region region, ServerInstance serverInstance) {
		List<Element> extFiles = new ArrayList<Element>();

		boolean hasDaytimeDependency = AlbinaUtil.hasDaytimeDependency(bulletins);
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTime = AlbinaUtil.getPublicationTime(bulletins);
		String baseUri = LinkUtil.getMapsUrl(lang, region, serverInstance) + "/" + validityDateString + "/" + publicationTime + "/";

		extFiles.add(createExtFile(doc, "link", lang.getBundleString("ext-file.website-link.description"),
				lang.getBundleString("website.url") + "/bulletin/" + validityDateString));
		extFiles.add(createExtFile(doc, "simple_link", lang.getBundleString("ext-file.simple-link.description"),
				LinkUtil.getSimpleHtmlUrl(lang, region, serverInstance) + "/" + validityDateString + "/" + lang.toString()
						+ ".html"));
		extFiles.add(createExtFile(doc, "fd_albina_map.jpg",
				LinkUtil.getExtFileMapDescription(lang, "fd", ""), baseUri +
				MapUtil.filename(region, MapLevel.standard, DaytimeDependency.fd, false, MapImageFormat.jpg)));
		extFiles.add(createExtFile(doc, "pdf", LinkUtil.getExtFilePdfDescription(lang, ""),
				baseUri + validityDateString + "_" + lang.toString() + ".pdf"));

		if (!hasDaytimeDependency) {
			extFiles.add(createExtFile(doc, "fd_overlay.png",
					LinkUtil.getExtFileOverlayDescription(lang, "fd"), baseUri +
					MapUtil.filename(region, MapLevel.overlay, DaytimeDependency.fd, false, MapImageFormat.png)));
		} else {
			extFiles.add(createExtFile(doc, "am_albina_map.jpg",
					LinkUtil.getExtFileMapDescription(lang, "am", ""), baseUri +
					MapUtil.filename(region, MapLevel.standard, DaytimeDependency.am, false, MapImageFormat.jpg)));
			extFiles.add(createExtFile(doc, "pm_albina_map.jpg",
					LinkUtil.getExtFileMapDescription(lang, "pm", ""), baseUri +
					MapUtil.filename(region, MapLevel.standard, DaytimeDependency.pm, false, MapImageFormat.jpg)));
			extFiles.add(createExtFile(doc, "am_overlay.png",
					LinkUtil.getExtFileOverlayDescription(lang, "am"), baseUri +
					MapUtil.filename(region, MapLevel.overlay, DaytimeDependency.am, false, MapImageFormat.png)));
			extFiles.add(createExtFile(doc, "pm_overlay.png",
					LinkUtil.getExtFileOverlayDescription(lang, "pm"), baseUri +
					MapUtil.filename(region, MapLevel.overlay, DaytimeDependency.pm, false, MapImageFormat.png)));
		}

		return extFiles;
	}

	public static Element createExtFile(Document doc, String id, String descr, String baseUri) {
		Element extFile = doc.createElement("extFile");
		Element typeElement = doc.createElement("type");
		typeElement.appendChild(doc.createTextNode(id));
		extFile.appendChild(typeElement);
		Element description = doc.createElement("description");
		description.appendChild(doc.createTextNode(descr));
		extFile.appendChild(description);
		Element fileReferenceURI = doc.createElement("fileReferenceURI");
		fileReferenceURI.appendChild(doc.createTextNode(baseUri));
		extFile.appendChild(fileReferenceURI);
		return extFile;
	}

	public static Document createXmlError(String key, String value) throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(key);
		rootElement.appendChild(doc.createTextNode(value));
		return doc;
	}

	public static String convertDocToString(Document doc) throws TransformerException {
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
