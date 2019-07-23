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
	public static void createCaamlFiles(List<AvalancheBulletin> bulletins) throws TransformerException, IOException {
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);

		String dirPathParent = GlobalVariables.getPdfDirectory() + validityDateString;
		String dirPath = GlobalVariables.getPdfDirectory() + validityDateString + "/" + publicationTimeString;
		new File(dirPath).mkdirs();

		// using PosixFilePermission to set file permissions 755
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		// add owners permission
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		// add group permissions
		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_EXECUTE);
		// add others permissions
		perms.add(PosixFilePermission.OTHERS_READ);
		perms.add(PosixFilePermission.OTHERS_EXECUTE);

		try {
			Files.setPosixFilePermissions(Paths.get(dirPathParent), perms);
			Files.setPosixFilePermissions(Paths.get(dirPath), perms);
		} catch (IOException | UnsupportedOperationException e) {
			logger.warn("File permissions could not be set!");
		}

		BufferedWriter writer;
		String fileName;

		Document docDe = XmlUtil.createCaaml(bulletins, LanguageCode.de);
		String caamlStringDe = XmlUtil.convertDocToString(docDe);
		fileName = dirPath + "/" + validityDateString + "_de.xml";
		writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(caamlStringDe);
		writer.close();
		AlbinaUtil.setFilePermissions(fileName);

		Document docIt = XmlUtil.createCaaml(bulletins, LanguageCode.it);
		String caamlStringIt = XmlUtil.convertDocToString(docIt);
		fileName = dirPath + "/" + validityDateString + "_it.xml";
		writer = new BufferedWriter(new FileWriter(dirPath + "/" + validityDateString + "_it.xml"));
		writer.write(caamlStringIt);
		writer.close();
		AlbinaUtil.setFilePermissions(fileName);

		Document docEn = XmlUtil.createCaaml(bulletins, LanguageCode.en);
		String caamlStringEn = XmlUtil.convertDocToString(docEn);
		fileName = dirPath + "/" + validityDateString + "_en.xml";
		writer = new BufferedWriter(new FileWriter(dirPath + "/" + validityDateString + "_en.xml"));
		writer.write(caamlStringEn);
		writer.close();
		AlbinaUtil.setFilePermissions(fileName);

		AlbinaUtil.runCopyXmlsScript(AlbinaUtil.getValidityDateString(bulletins),
				AlbinaUtil.getPublicationTime(bulletins));
	}

	public static Document createCaaml(List<AvalancheBulletin> bulletins, LanguageCode language) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = XmlUtil.createObsCollectionHeaderCaaml(doc);

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

				Element metaDataProperty = doc.createElement("metaDataProperty");
				Element metaData = doc.createElement("MetaData");
				Element dateTimeReport = doc.createElement("dateTimeReport");
				dateTimeReport.appendChild(doc.createTextNode(
						publicationDate.withZone(DateTimeZone.UTC).toString(GlobalVariables.formatterDateTime)));
				metaData.appendChild(dateTimeReport);

				metaDataProperty.appendChild(metaData);
				rootElement.appendChild(metaDataProperty);

				Element observations = doc.createElement("observations");

				for (AvalancheBulletin bulletin : bulletins) {
					List<Element> caaml = bulletin.toCAAML(doc, language);
					if (caaml != null)
						for (Element element : caaml) {
							if (element != null)
								observations.appendChild(element);
						}
				}
				rootElement.appendChild(observations);

			}

			doc.appendChild(rootElement);

			return doc;
		} catch (ParserConfigurationException e1) {
			logger.error("Error producing CAAML: " + e1.getMessage());
			e1.printStackTrace();
			return null;
		}
	}

	public static Element createObsCollectionHeaderCaaml(Document doc) {
		Element rootElement = doc.createElement("ObsCollection");
		rootElement.setAttribute("xmlns", "http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS");
		rootElement.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
		rootElement.setAttribute("xmlns:albina", GlobalVariables.albinaXmlSchemaUrl);
		rootElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xmlns:schemaLocation",
				"http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS/CAAMLv5_BulletinEAWS.xsd");
		rootElement.setAttribute("xmlns:app", "ALBINA");

		return rootElement;
	}

	public static String createValidElevationAttribute(int elevation, boolean above, boolean treeline) {
		// TODO Allow treeline in CAAML
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
		Element rootElement = doc.createElement("LocationCollection");
		rootElement.setAttribute("xmlns", "http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS");
		rootElement.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
		rootElement.setAttribute("xmlns:app", "ALBINA");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xmlns:schemaLocation",
				"http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS/CAAMLv5_BulletinEAWS.xsd");
		doc.appendChild(rootElement);

		Element metaDataProperty = doc.createElement("metaDataProperty");
		Element metaData = doc.createElement("MetaData");
		Element dateTimeReport = doc.createElement("dateTimeReport");
		dateTimeReport.appendChild(doc.createTextNode((new DateTime()).toString(GlobalVariables.formatterDateTime)));
		metaData.appendChild(dateTimeReport);
		Element srcRef = doc.createElement("srcRef");
		Element operation = doc.createElement("Operation");
		operation.setAttribute("gml:id", "ALBINA");
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode("ALBINA"));
		operation.appendChild(name);
		srcRef.appendChild(operation);
		metaData.appendChild(srcRef);
		metaDataProperty.appendChild(metaData);
		rootElement.appendChild(metaDataProperty);
		return rootElement;
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
