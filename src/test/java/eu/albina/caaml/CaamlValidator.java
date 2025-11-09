// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.caaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Verify;
import com.google.common.io.Resources;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
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
			Verify.verify(validateCAAMLv6(caamlString).isEmpty());
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

	static Set<ValidationMessage> validateCAAMLv6(String caaml) {
		try {
			JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
			JsonSchema jsonSchema1 = jsonSchemaFactory.getSchema(Resources.getResource("CAAMLv6_BulletinEAWS.json").openStream());
			return jsonSchema1.validate(new ObjectMapper().readTree(caaml));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
