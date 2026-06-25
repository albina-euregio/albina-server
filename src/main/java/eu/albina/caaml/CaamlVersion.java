package eu.albina.caaml;

import org.w3c.dom.Element;

/**
 * The version of CAAML (Canadian Avalanche Association Markup Language)
 * @apiNote http://caaml.org/
 */
public enum CaamlVersion {

	V6, V6_JSON;

	public interface Str {
		String V6 = "V6";
		String V6_JSON = "V6_JSON";
	}

	String filenameSuffix() {
		return switch (this) {
			case V6 -> "_CAAMLv6.xml";
			case V6_JSON -> "_CAAMLv6.json";
		};
	}

	int version() {
		return 6;
	}

	String namespace() {
		return "http://caaml.org/Schemas/V6.0/Profiles/BulletinEAWS";
	}

	String schemaLocation() {
		return "http://caaml.org/Schemas/BulletinEAWS/v6.0/xml/CAAMLv6_BulletinEAWS.xsd";
	}

	Element setNamespaceAttributes(Element rootElement) {
		rootElement.setAttribute("xmlns", namespace());
		rootElement.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
		rootElement.setAttribute("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
		rootElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xsi:schemaLocation", namespace() + " " + schemaLocation());
		return rootElement;
	}
}
