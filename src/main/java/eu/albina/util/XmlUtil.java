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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.caaml.CaamlVersion;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class XmlUtil {

	private static final Logger logger = LoggerFactory.getLogger(XmlUtil.class);

	public static void createCaamlFiles(List<AvalancheBulletin> bulletins, String validityDateString,
			String publicationTimeString, CaamlVersion version) throws TransformerException, IOException {
		String dirPathParent = GlobalVariables.getPdfDirectory() + "/" + validityDateString;
		String dirPath = GlobalVariables.getPdfDirectory() + "/" + validityDateString + "/" + publicationTimeString;
		new File(dirPath).mkdirs();

		// using PosixFilePermission to set file permissions 777
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		// add owners permission
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		// add group permissions
		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_WRITE);
		perms.add(PosixFilePermission.GROUP_EXECUTE);
		// add others permissions
		perms.add(PosixFilePermission.OTHERS_READ);
		perms.add(PosixFilePermission.OTHERS_WRITE);
		perms.add(PosixFilePermission.OTHERS_EXECUTE);

		try {
			Files.setPosixFilePermissions(Paths.get(dirPathParent), perms);
			Files.setPosixFilePermissions(Paths.get(dirPath), perms);
		} catch (IOException | UnsupportedOperationException e) {
			logger.warn("File permissions could not be set!");
		}

		for (LanguageCode lang : LanguageCode.ENABLED) {
			Document doc = createCaaml(bulletins, lang, version);
			String caamlString = XmlUtil.convertDocToString(doc);
			String fileName;
			if (version == CaamlVersion.V5)
				fileName = dirPath + "/" + validityDateString + "_" + lang.toString() + ".xml";
			else
				fileName = dirPath + "/" + validityDateString + "_" + lang.toString() + "_CAAMLv6.xml";
			Files.write(Paths.get(fileName), caamlString.getBytes(StandardCharsets.UTF_8));
			AlbinaUtil.setFilePermissions(fileName);
		}
	}

	public static Document createCaaml(List<AvalancheBulletin> bulletins, LanguageCode lang, CaamlVersion version) {
		if (version == CaamlVersion.V5) {
			return XmlUtil.createCaamlv5(bulletins, lang);
		} else {
			return XmlUtil.createCaamlv6(bulletins, lang);
		}
	}

	public static Document createCaamlv5(List<AvalancheBulletin> bulletins, LanguageCode language) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = CaamlVersion.V5.setNamespaceAttributes(doc.createElement("ObsCollection"));

			// create meta data
			ZonedDateTime publicationDate = null;
			if (bulletins != null && !bulletins.isEmpty()) {
				for (AvalancheBulletin bulletin : bulletins) {
					if (bulletin.getPublicationDate() != null) {
						if (publicationDate == null)
							publicationDate = bulletin.getPublicationDate();
						else {
							if (bulletin.getPublicationDate().isAfter(publicationDate))
								publicationDate = bulletin.getPublicationDate();
						}
					}
				}

				// metaData
				Element metaDataProperty = createMetaDataProperty(doc, publicationDate);
				rootElement.appendChild(metaDataProperty);

				// observations
				Element observations = doc.createElement("observations");

				for (AvalancheBulletin bulletin : bulletins) {
					List<Element> caaml = bulletin.toCAAMLv5(doc, language);
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

			return doc;
		} catch (ParserConfigurationException e1) {
			logger.error("Error producing CAAML", e1);
			return null;
		}
	}

	public static Document createCaamlv6(List<AvalancheBulletin> bulletins, LanguageCode language) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = CaamlVersion.V6.setNamespaceAttributes(doc.createElement("bulletins"));

			// create meta data
			if (bulletins != null && !bulletins.isEmpty()) {

				// metaData
				Element metaData = doc.createElement("metaData");
				for (Element extFile : createObsCollectionExtFiles(doc, bulletins, language)) {
					metaData.appendChild(extFile);
				}
				rootElement.appendChild(metaData);

				String reportPublicationTime = AlbinaUtil.getPublicationTime(bulletins);

				for (AvalancheBulletin bulletin : bulletins) {
					List<Element> caaml = bulletin.toCAAMLv6(doc, language, reportPublicationTime);
					if (caaml != null)
						for (Element element : caaml) {
							if (element != null)
								rootElement.appendChild(element);
						}
				}
			}

			doc.appendChild(rootElement);

			return doc;
		} catch (ParserConfigurationException e1) {
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

	public static Element createMetaDataProperty(Document doc, ZonedDateTime dateTime) {
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
		name.appendChild(doc.createTextNode("Avalanche.report"));
		operation.appendChild(name);
		srcRef.appendChild(operation);
		metaData.appendChild(srcRef);

		metaDataProperty.appendChild(metaData);
		return metaDataProperty;
	}

	private static List<Element> createObsCollectionExtFiles(Document doc, List<AvalancheBulletin> bulletins, LanguageCode lang) {
		List<Element> extFiles = new ArrayList<Element>();

		boolean hasDaytimeDependency = AlbinaUtil.hasDaytimeDependency(bulletins);
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTime = AlbinaUtil.getPublicationTime(bulletins);
		String baseUri = GlobalVariables.getMapsUrl(lang) + "/" + validityDateString + "/" + publicationTime + "/";

		extFiles.add(createExtFile(doc, "link", lang.getBundleString("ext-file.website-link.description"),
				lang.getBundleString("avalanche-report.url") + "/bulletin/" + validityDateString));
		extFiles.add(createExtFile(doc, "simple_link", lang.getBundleString("ext-file.simple-link.description"),
				GlobalVariables.getAvalancheReportSimpleBaseUrl(lang) + validityDateString + "/" + lang.toString()
						+ ".html"));
		extFiles.add(createExtFile(doc, "fd_albina_map.jpg",
				GlobalVariables.getExtFileMapDescription(lang, "fd", ""), baseUri + "fd_albina_map.jpg"));
		extFiles.add(createExtFile(doc, "fd_tyrol_map.jpg",
				GlobalVariables.getExtFileMapDescription(lang, "fd", GlobalVariables.codeTyrol),
				baseUri + "fd_tyrol_map.jpg"));
		extFiles.add(createExtFile(doc, "fd_southtyrol_map.jpg",
				GlobalVariables.getExtFileMapDescription(lang, "fd", GlobalVariables.codeSouthTyrol),
				baseUri + "fd_southtyrol_map.jpg"));
		extFiles.add(createExtFile(doc, "fd_trentino_map.jpg",
				GlobalVariables.getExtFileMapDescription(lang, "fd", GlobalVariables.codeTrentino),
				baseUri + "fd_trentino_map.jpg"));
		extFiles.add(createExtFile(doc, "pdf", GlobalVariables.getExtFilePdfDescription(lang, ""),
				baseUri + validityDateString + "_" + lang.toString() + ".pdf"));
		extFiles.add(createExtFile(doc, "tyrol_pdf",
				GlobalVariables.getExtFilePdfDescription(lang, GlobalVariables.codeTyrol),
				baseUri + validityDateString + "_" + GlobalVariables.codeTyrol + "_" + lang.toString() + ".pdf"));
		extFiles.add(createExtFile(doc, "southtyrol_pdf",
				GlobalVariables.getExtFilePdfDescription(lang, GlobalVariables.codeSouthTyrol),
				baseUri + validityDateString + "_" + GlobalVariables.codeSouthTyrol + "_" + lang.toString() + ".pdf"));
		extFiles.add(createExtFile(doc, "trentino_pdf",
				GlobalVariables.getExtFilePdfDescription(lang, GlobalVariables.codeTrentino),
				baseUri + validityDateString + "_" + GlobalVariables.codeTrentino + "_" + lang.toString() + ".pdf"));

		if (!hasDaytimeDependency) {
			extFiles.add(createExtFile(doc, "fd_overlay.png",
					GlobalVariables.getExtFileOverlayDescription(lang, "fd"), baseUri + "fd_overlay.png"));
			extFiles.add(createExtFile(doc, "fd_regions.json",
					GlobalVariables.getExtFileRegionsDescription(lang, "fd"), baseUri + "fd_regions.json"));
		} else {
			extFiles.add(createExtFile(doc, "am_albina_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "am", ""), baseUri + "am_albina_map.jpg"));
			extFiles.add(createExtFile(doc, "am_tyrol_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "am", GlobalVariables.codeTyrol),
					baseUri + "am_tyrol_map.jpg"));
			extFiles.add(createExtFile(doc, "am_southtyrol_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "am", GlobalVariables.codeSouthTyrol),
					baseUri + "am_southtyrol_map.jpg"));
			extFiles.add(createExtFile(doc, "am_trentino_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "am", GlobalVariables.codeTrentino),
					baseUri + "am_trentino_map.jpg"));
			extFiles.add(createExtFile(doc, "pm_albina_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "pm", ""), baseUri + "pm_albina_map.jpg"));
			extFiles.add(createExtFile(doc, "pm_tyrol_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "pm", GlobalVariables.codeTyrol),
					baseUri + "pm_tyrol_map.jpg"));
			extFiles.add(createExtFile(doc, "pm_southtyrol_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "pm", GlobalVariables.codeSouthTyrol),
					baseUri + "pm_southtyrol_map.jpg"));
			extFiles.add(createExtFile(doc, "pm_trentino_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "pm", GlobalVariables.codeTrentino),
					baseUri + "pm_trentino_map.jpg"));
			extFiles.add(createExtFile(doc, "am_overlay.png",
					GlobalVariables.getExtFileOverlayDescription(lang, "am"), baseUri + "am_overlay.png"));
			extFiles.add(createExtFile(doc, "pm_overlay.png",
					GlobalVariables.getExtFileOverlayDescription(lang, "pm"), baseUri + "pm_overlay.png"));
			extFiles.add(createExtFile(doc, "am_regions.json",
					GlobalVariables.getExtFileRegionsDescription(lang, "am"), baseUri + "am_regions.json"));
			extFiles.add(createExtFile(doc, "pm_regions.json",
					GlobalVariables.getExtFileRegionsDescription(lang, "pm"), baseUri + "pm_regions.json"));
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
