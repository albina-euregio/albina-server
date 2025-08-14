// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.exception;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.openjson.JSONObject;

import eu.albina.model.AvalancheInformationObject;

/**
 * Custom exception for the ALBINA project.
 *
 * @author Norbert Lanzanasto
 *
 */
public class AlbinaException extends Exception implements AvalancheInformationObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 3770001034138464030L;

	/**
	 * Constructor with a custom {@code message} text.
	 *
	 * @param message
	 *            the message text for the exception.
	 */
	public AlbinaException(String message) {
		super(message);
	}

	/**
	 * Return a XML string of the exception.
	 *
	 * @return a XML string of the exception
	 * @throws ParserConfigurationException
	 *             if the XML builder could not be created
	 * @throws TransformerFactoryConfigurationError
	 *             if the XML transformer could be created
	 * @throws TransformerException
	 *             if the exception could not be transformed to XML
	 */
	public String toXML()
			throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("message");
		rootElement.appendChild(doc.createTextNode(this.getMessage()));
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StringWriter stringWriter = new StringWriter();
		StreamResult result = new StreamResult(stringWriter);
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		return stringWriter.toString();
	}

	/**
	 * Return a {@code JSONObject} representing the exception (only containing the
	 * message).
	 *
	 * @return a {@code JSONObject} representing the exception (only containing the
	 *         message)
	 */
	@Override
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		result.put("message", this.getMessage());
		return result;
	}

}
