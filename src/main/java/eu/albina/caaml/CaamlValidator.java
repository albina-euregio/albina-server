// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.caaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import com.google.common.base.Verify;
import com.google.common.io.Resources;
import eu.albina.json.JsonValidator;
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
		if (version == CaamlVersion.V6) {
			try (InputStream xsd = Resources.getResource("CAAMLv6_BulletinEAWS.xsd").openStream()) {
				return validate(caamlString, new StreamSource(xsd));
			}
		} else if (version == CaamlVersion.V6_JSON) {
			Verify.verify(JsonValidator.validateCAAMLv6(caamlString).isEmpty());
			return true;
		}
		return validate(caamlString, new StreamSource(version.schemaLocation()));
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
