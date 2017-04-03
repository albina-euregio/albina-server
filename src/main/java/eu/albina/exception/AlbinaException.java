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

public class AlbinaException extends Exception implements AvalancheInformationObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3770001034138464030L;

	public AlbinaException(String message) {
		super(message);
	}

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

	@Override
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		result.put("message", this.getMessage());
		return result;
	}

}
