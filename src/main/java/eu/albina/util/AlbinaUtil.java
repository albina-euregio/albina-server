package eu.albina.util;

import java.io.StringWriter;

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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.model.enumerations.DangerRating;

public class AlbinaUtil {

	private static final Logger logger = LoggerFactory.getLogger(AlbinaUtil.class);

	public static final boolean createCAAML = true;
	public static final boolean createMaps = false;
	public static final boolean publishToSocialMedia = false;

	public static final String dangerRatingColorLow = "#CCFF66";
	public static final String dangerRatingColorModerate = "#FFFF00";
	public static final String dangerRatingColorConsiderable = "#FF9900";
	public static final String dangerRatingColorHigh = "#FF0000";
	public static final String dangerRatingColorVeryHigh = "#800000";
	public static final String dangerRatingColorMissing = "#969696";

	public static String getDangerRatingColor(DangerRating dangerRating) {
		switch (dangerRating) {
		case low:
			return dangerRatingColorLow;
		case moderate:
			return dangerRatingColorModerate;
		case considerable:
			return dangerRatingColorConsiderable;
		case high:
			return dangerRatingColorHigh;
		case very_high:
			return dangerRatingColorVeryHigh;
		default:
			return dangerRatingColorMissing;
		}
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

	public static Document createXmlError(String key, String value) throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(key);
		rootElement.appendChild(doc.createTextNode(value));
		return doc;
	}

	public static JSONObject createRegionHeaderJson() {
		JSONObject json = new JSONObject();
		json.put("type", "FeatureCollection");
		JSONObject crs = new JSONObject();
		crs.put("type", "name");
		JSONObject properties = new JSONObject();
		properties.put("name", GlobalVariables.referenceSystemUrn);
		crs.put("properties", properties);
		json.put("crs", crs);
		return json;
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

	public static Element createObsCollectionHeaderCaaml(Document doc) {
		Element rootElement = doc.createElement("ObsCollection");
		rootElement.setAttribute("xmlns", "http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS");
		rootElement.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
		rootElement.setAttribute("xmlns:albina", "http://212.47.231.185:8080/caaml/albina.xsd");
		rootElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xmlns:schemaLocation",
				"http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS/CAAMLv5_BulletinEAWS.xsd");
		rootElement.setAttribute("xmlns:app", "ALBINA");

		return rootElement;
	}

	public static String createValidElevationAttribute(int elevation, boolean above, boolean treeline) {
		// TODO allow treeline in CAAML
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
}
