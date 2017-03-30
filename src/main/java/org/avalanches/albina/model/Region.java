package org.avalanches.albina.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This class holds all information about one region.
 * 
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "REGIONS")
public class Region extends AbstractPersistentObject implements AvalancheInformationObject {

	@Column(name = "NAME")
	private String name;

	@Column(name = "POLYGON")
	private Polygon polygon;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "PARENTREGION_ID")
	private Region parentRegion;

	@OneToMany(mappedBy = "parentRegion")
	private Set<Region> subregions;

	/**
	 * Default constructor. Initializes all collections of the region.
	 */
	public Region() {
		subregions = new HashSet<Region>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}

	public Region getParentRegion() {
		return parentRegion;
	}

	public void setParentRegion(Region parentRegion) {
		this.parentRegion = parentRegion;
	}

	public Set<Region> getSubregions() {
		return subregions;
	}

	public void setSubregions(Set<Region> subregions) {
		this.subregions = subregions;
	}

	public Document toCAAML() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
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
			// TODO use datetimeformatter from global variables
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
			dateTimeReport.appendChild(doc.createTextNode(dt.format(new Date())));
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

			Element locations = doc.createElement("locations");
			locations.appendChild(regionToCaaml(this, doc));
			for (Region subregion : subregions)
				locations.appendChild(regionToCaaml(subregion, doc));

			rootElement.appendChild(locations);

			return doc;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private Element regionToCaaml(Region subregion, Document doc) {
		Element region = doc.createElement("Region");
		region.setAttribute("gml:id", "R" + subregion.getId());
		Element regionname = doc.createElement("name");
		regionname.appendChild(doc.createTextNode(subregion.name));
		region.appendChild(regionname);
		Element regionSubType = doc.createElement("regionSubType");
		region.appendChild(regionSubType);
		Element outline = doc.createElement("outline");
		Element polygon = doc.createElement("gml:Polygon");
		polygon.setAttribute("gml:id", "P" + subregion.getId());
		polygon.setAttribute("srsDimension", "2");
		polygon.setAttribute("srsName", "urn:ogc:def:crs:OGC:1.3:CRS84");
		Element exterior = doc.createElement("gml:exterior");
		Element linearRing = doc.createElement("gml:LinearRing");
		Element posList = doc.createElement("gml:posList");

		if (subregion.polygon != null && subregion.polygon.getCoordinates() != null) {
			StringBuilder sb = new StringBuilder();
			for (Coordinate coordinate : subregion.polygon.getCoordinates())
				sb.append(coordinate.x + " " + coordinate.y + " ");
			posList.appendChild(doc.createTextNode(sb.toString()));
		}

		linearRing.appendChild(posList);
		exterior.appendChild(linearRing);
		polygon.appendChild(exterior);
		outline.appendChild(polygon);
		region.appendChild(outline);
		return region;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		json.put("type", "FeatureCollection");
		JSONObject crs = new JSONObject();
		crs.put("type", "name");
		JSONObject properties = new JSONObject();
		properties.put("name", "urn:ogc:def:crs:OGC:1.3:CRS84");
		crs.put("properties", properties);
		json.put("crs", crs);

		JSONArray features = new JSONArray();
		features.put(regionToJson(this));
		for (Region subregion : subregions)
			features.put(regionToJson(subregion));

		json.put("features", features);

		return json;
	}

	private JSONObject regionToJson(Region region) {
		JSONObject feature = new JSONObject();

		feature.put("type", "Feature");
		JSONObject featureProperties = new JSONObject();
		featureProperties.put("name", region.name);
		featureProperties.put("id", region.getId());
		feature.put("properties", featureProperties);

		JSONObject geometry = new JSONObject();
		geometry.put("type", "Polygon");
		JSONArray coordinates = new JSONArray();
		JSONArray innerCoordinates = new JSONArray();

		if (region.polygon != null && region.polygon.getCoordinates() != null) {
			for (Coordinate coordinate : region.polygon.getCoordinates()) {
				JSONArray entry = new JSONArray();
				entry.put(coordinate.x);
				entry.put(coordinate.y);
				innerCoordinates.put(entry);
			}
		}

		coordinates.put(innerCoordinates);
		geometry.put("coordinates", coordinates);
		feature.put("geometry", geometry);
		return feature;
	}
}
