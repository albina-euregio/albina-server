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

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		return result.getWriter().toString();
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
