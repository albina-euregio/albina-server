package eu.albina.util;

import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public interface XmlUtil {
	static String convertDocToString(Document doc) throws TransformerException {
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
