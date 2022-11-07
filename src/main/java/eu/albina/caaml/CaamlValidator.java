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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public interface CaamlValidator {

	Logger logger = LoggerFactory.getLogger(CaamlValidator.class);

	/**
	 * Validates a CAAML string against the CAAML Bulletin schema.
	 *
	 * @param caamlString
	 *            The CAAML string to be validated.
	 * @param type
	 *            The type of the schema (snowProfile, avalancheBulletin or
	 *            avalancheIncident).
	 * @return Returns a JSON object containing the error messages (or empty for
	 *         success).
	 * @throws IOException
	 * @throws SAXException
	 *             If the caamlString is not valid.
	 */
	static boolean validateCaamlBulletin(String caamlString, CaamlVersion version)
			throws SAXException, IOException {
		return validate(caamlString, new StreamSource(version.schemaLocation()));
	}

	static boolean validateCaamlBulletinLocalV5(String caamlString) throws SAXException, IOException {
		return validate(caamlString, new StreamSource(
				new File("D:\\norbert\\workspaces\\albina-euregio\\albina-caaml\\5.0\\CAAMLv5_BulletinEAWS.xsd")));
	}

	static boolean validateCaamlBulletinLocalV6(String caamlString) throws SAXException, IOException {
		return validate(caamlString, new StreamSource(
				new File("D:\\norbert\\workspaces\\albina-euregio\\albina-caaml\\6.0\\CAAMLv6_BulletinEAWS.xsd")));
	}

	static boolean validate(String caamlString, Source schemaFile) throws SAXException, IOException {
		StringReader stringReader = new StringReader(caamlString);
		Source xmlFile = new StreamSource(stringReader);
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(schemaFile);
		Validator validator = schema.newValidator();
		validator.validate(xmlFile);
		logger.debug("CAAML is valid!");
		return true;
	}
}
