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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
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

import eu.albina.caaml.CaamlVersion;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class XmlUtil {

	private static final Logger logger = LoggerFactory.getLogger(XmlUtil.class);

	// LANG
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

		BufferedWriter writer;
		String fileName;

		Document docDe = XmlUtil.createCaaml(bulletins, LanguageCode.de, version);
		String caamlStringDe = XmlUtil.convertDocToString(docDe);
		fileName = dirPath + "/" + validityDateString + "_de.xml";
		writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(caamlStringDe);
		writer.close();
		AlbinaUtil.setFilePermissions(fileName);

		Document docIt = XmlUtil.createCaaml(bulletins, LanguageCode.it, version);
		String caamlStringIt = XmlUtil.convertDocToString(docIt);
		fileName = dirPath + "/" + validityDateString + "_it.xml";
		writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(caamlStringIt);
		writer.close();
		AlbinaUtil.setFilePermissions(fileName);

		Document docEn = XmlUtil.createCaaml(bulletins, LanguageCode.en, version);
		String caamlStringEn = XmlUtil.convertDocToString(docEn);
		fileName = dirPath + "/" + validityDateString + "_en.xml";
		writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(caamlStringEn);
		writer.close();
		AlbinaUtil.setFilePermissions(fileName);
	}

	public static Document createCaaml(List<AvalancheBulletin> bulletins, LanguageCode language, CaamlVersion version) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = version.setNamespaceAttributes(doc.createElement("ObsCollection"));

			// create meta data
			DateTime publicationDate = null;
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
				if (version == CaamlVersion.V5) {
					Element metaDataProperty = createMetaDataProperty(doc, publicationDate);
					rootElement.appendChild(metaDataProperty);
				} else {
					Element metaData = doc.createElement("metaData");
					Element dateTimeReport = doc.createElement("dateTimeReport");
					dateTimeReport.appendChild(doc.createTextNode(
						publicationDate.withZone(DateTimeZone.UTC).toString(GlobalVariables.formatterDateTime)));
					metaData.appendChild(dateTimeReport);
					rootElement.appendChild(metaData);
					metaData.appendChild(createObsCollectionExtFiles(doc, bulletins, language));
				}


				// observations
				Element observations = doc.createElement("observations");

				for (AvalancheBulletin bulletin : bulletins) {
					List<Element> caaml = bulletin.toCAAML(doc, language, version);
					if (caaml != null)
						for (Element element : caaml) {
							if (element != null)
								observations.appendChild(element);
						}
				}
				rootElement.appendChild(observations);

				// attributes
				if (version != CaamlVersion.V5) {
					if (language == null)
						language = LanguageCode.en;
					rootElement.setAttribute("xml:lang", language.toString());
				}
			}

			doc.appendChild(rootElement);

			return doc;
		} catch (ParserConfigurationException e1) {
			logger.error("Error producing CAAML", e1);
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

	public static Element createRegionHeaderCaaml(Document doc) throws ParserConfigurationException {
		Element rootElement = CaamlVersion.V5.setNamespaceAttributes(doc.createElement("LocationCollection"));
		doc.appendChild(rootElement);

		Element metaDataProperty = createMetaDataProperty(doc, new DateTime().withTimeAtStartOfDay());
		rootElement.appendChild(metaDataProperty);
		return rootElement;
	}

	public static Element createMetaDataProperty(Document doc, DateTime dateTime) {
		Element metaDataProperty = doc.createElement("metaDataProperty");
		Element metaData = doc.createElement("MetaData");
		Element dateTimeReport = doc.createElement("dateTimeReport");
		dateTimeReport.appendChild(
				doc.createTextNode(dateTime.withZone(DateTimeZone.UTC).toString(GlobalVariables.formatterDateTime)));
		metaData.appendChild(dateTimeReport);
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

	private static Element createObsCollectionExtFiles(Document doc, List<AvalancheBulletin> bulletins,
			LanguageCode lang) {
		Element extFiles = doc.createElement("extFiles");

		boolean hasDaytimeDependency = AlbinaUtil.hasDaytimeDependency(bulletins);
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTime = AlbinaUtil.getPublicationTime(bulletins);
		String baseUri = GlobalVariables.getMapsUrl(lang) + "/" + validityDateString + "/" + publicationTime + "/";

		extFiles.appendChild(createExtFile(doc, "link", GlobalVariables.getExtFileLinkDescription(lang),
				GlobalVariables.getAvalancheReportBaseUrl(lang) + "bulletin/" + validityDateString));
		extFiles.appendChild(createExtFile(doc, "simple_link", GlobalVariables.getExtFileSimpleLinkDescription(lang),
				GlobalVariables.getAvalancheReportSimpleBaseUrl(lang) + validityDateString + "/" + lang.toString()
						+ ".html"));
		extFiles.appendChild(createExtFile(doc, "fd_albina_map.jpg",
				GlobalVariables.getExtFileMapDescription(lang, "fd", ""), baseUri + "fd_albina_map.jpg"));
		extFiles.appendChild(createExtFile(doc, "fd_tyrol_map.jpg",
				GlobalVariables.getExtFileMapDescription(lang, "fd", "AT-07"), baseUri + "fd_tyrol_map.jpg"));
		extFiles.appendChild(createExtFile(doc, "fd_southtyrol_map.jpg",
				GlobalVariables.getExtFileMapDescription(lang, "fd", "IT-32-BZ"), baseUri + "fd_southtyrol_map.jpg"));
		extFiles.appendChild(createExtFile(doc, "fd_trentino_map.jpg",
				GlobalVariables.getExtFileMapDescription(lang, "fd", "IT-32-TN"), baseUri + "fd_trentino_map.jpg"));
		extFiles.appendChild(createExtFile(doc, "pdf", GlobalVariables.getExtFilePdfDescription(lang, ""),
				baseUri + validityDateString + "_" + lang.toString() + ".pdf"));
		extFiles.appendChild(createExtFile(doc, "tyrol_pdf", GlobalVariables.getExtFilePdfDescription(lang, "AT-07"),
				baseUri + validityDateString + "_AT-07_" + lang.toString() + ".pdf"));
		extFiles.appendChild(
				createExtFile(doc, "southtyrol_pdf", GlobalVariables.getExtFilePdfDescription(lang, "IT-32-BZ"),
						baseUri + validityDateString + "_IT-32_BZ" + lang.toString() + ".pdf"));
		extFiles.appendChild(
				createExtFile(doc, "trentino_pdf", GlobalVariables.getExtFilePdfDescription(lang, "IT-32-TN"),
						baseUri + validityDateString + "_IT-32-TN_" + lang.toString() + ".pdf"));

		if (!hasDaytimeDependency) {
			extFiles.appendChild(createExtFile(doc, "fd_overlay.png",
					GlobalVariables.getExtFileOverlayDescription(lang, "fd"), baseUri + "fd_overlay.png"));
			extFiles.appendChild(createExtFile(doc, "fd_regions.json",
					GlobalVariables.getExtFileRegionsDescription(lang, "fd"), baseUri + "fd_regions.json"));
		} else {
			extFiles.appendChild(createExtFile(doc, "am_albina_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "am", ""), baseUri + "am_albina_map.jpg"));
			extFiles.appendChild(createExtFile(doc, "am_tyrol_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "am", "AT-07"), baseUri + "am_tyrol_map.jpg"));
			extFiles.appendChild(createExtFile(doc, "am_southtyrol_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "am", "IT-32-BZ"),
					baseUri + "am_southtyrol_map.jpg"));
			extFiles.appendChild(createExtFile(doc, "am_trentino_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "am", "IT-32-TN"), baseUri + "am_trentino_map.jpg"));
			extFiles.appendChild(createExtFile(doc, "pm_albina_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "pm", ""), baseUri + "pm_albina_map.jpg"));
			extFiles.appendChild(createExtFile(doc, "pm_tyrol_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "pm", "AT-07"), baseUri + "pm_tyrol_map.jpg"));
			extFiles.appendChild(createExtFile(doc, "pm_southtyrol_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "pm", "IT-32-BZ"),
					baseUri + "pm_southtyrol_map.jpg"));
			extFiles.appendChild(createExtFile(doc, "pm_trentino_map.jpg",
					GlobalVariables.getExtFileMapDescription(lang, "pm", "IT-32-TN"), baseUri + "pm_trentino_map.jpg"));
			extFiles.appendChild(createExtFile(doc, "am_overlay.png",
					GlobalVariables.getExtFileOverlayDescription(lang, "am"), baseUri + "am_overlay.png"));
			extFiles.appendChild(createExtFile(doc, "pm_overlay.png",
					GlobalVariables.getExtFileOverlayDescription(lang, "pm"), baseUri + "pm_overlay.png"));
			extFiles.appendChild(createExtFile(doc, "am_regions.json",
					GlobalVariables.getExtFileRegionsDescription(lang, "am"), baseUri + "am_regions.json"));
			extFiles.appendChild(createExtFile(doc, "pm_regions.json",
					GlobalVariables.getExtFileRegionsDescription(lang, "pm"), baseUri + "pm_regions.json"));
		}

		return extFiles;
	}

	public static Element createExtFile(Document doc, String id, String descr, String baseUri) {
		Element extFile = doc.createElement("extFile");
		Element description = doc.createElement("description");
		description.appendChild(doc.createTextNode(descr));
		extFile.appendChild(description);
		Element fileReferenceURI = doc.createElement("fileReferenceURI");
		fileReferenceURI.appendChild(doc.createTextNode(baseUri));
		extFile.appendChild(fileReferenceURI);
		extFile.setAttribute("gml:id", id);
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
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		return result.getWriter().toString();
	}
}
