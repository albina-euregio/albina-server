package org.avalanches.albina.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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

	public Element toCAAML(Document doc) {
		Element region = doc.createElement("Region");
		region.setAttribute("gml:id", "R" + getId());
		Element regionname = doc.createElement("name");
		regionname.appendChild(doc.createTextNode(name));
		region.appendChild(regionname);
		Element regionSubType = doc.createElement("regionSubType");
		region.appendChild(regionSubType);
		Element outline = doc.createElement("outline");
		Element polygon = doc.createElement("gml:Polygon");
		polygon.setAttribute("gml:id", "P" + getId());
		polygon.setAttribute("srsDimension", "2");
		polygon.setAttribute("srsName", "urn:ogc:def:crs:OGC:1.3:CRS84");
		Element exterior = doc.createElement("gml:exterior");
		Element linearRing = doc.createElement("gml:LinearRing");
		Element posList = doc.createElement("gml:posList");

		if (this.polygon != null && this.polygon.getCoordinates() != null) {
			StringBuilder sb = new StringBuilder();
			for (Coordinate coordinate : this.polygon.getCoordinates())
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
		JSONObject feature = new JSONObject();

		feature.put("type", "Feature");
		JSONObject featureProperties = new JSONObject();
		featureProperties.put("name", name);
		featureProperties.put("id", getId());
		feature.put("properties", featureProperties);

		JSONObject geometry = new JSONObject();
		geometry.put("type", "Polygon");
		JSONArray coordinates = new JSONArray();
		JSONArray innerCoordinates = new JSONArray();

		if (polygon != null && polygon.getCoordinates() != null) {
			for (Coordinate coordinate : polygon.getCoordinates()) {
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
