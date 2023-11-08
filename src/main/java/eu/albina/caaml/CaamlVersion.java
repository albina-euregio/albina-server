package eu.albina.caaml;

import org.w3c.dom.Element;

/**
 * The version of CAAML (Canadian Avalanche Association Markup Language)
 * @apiNote http://caaml.org/
 */
public enum CaamlVersion {

	V5, V6, V6_JSON;

	String filenameSuffix() {
		if (this == V5)
			return ".xml";
		else if (this == V6_JSON)
			return "_CAAMLv6.json";
		else
			return "_CAAMLv6.xml";
	}

	int version() {
		return this == V5 ? 5 : 6;
	}

	String namespace() {
		return this == V5 ? "http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS"
				: "http://caaml.org/Schemas/V6.0/Profiles/BulletinEAWS";
	}

	String schemaLocation() {
		return this == V5 ? "http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS/CAAMLv5_BulletinEAWS.xsd"
				: "http://caaml.org/Schemas/BulletinEAWS/v6.0/xml/CAAMLv6_BulletinEAWS.xsd";
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
