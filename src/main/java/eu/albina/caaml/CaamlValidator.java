package eu.albina.caaml;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eu.albina.util.GlobalVariables;

public class CaamlValidator {

	private static Logger logger = LoggerFactory.getLogger(CaamlValidator.class);

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
	public static boolean validateCaamlBulletin(String caamlString) throws SAXException, IOException {
		String bulletinCaamlSchemaString = GlobalVariables.getBulletinCaamlSchemaFileString();
		URL schemaFile = new URL(bulletinCaamlSchemaString);
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
