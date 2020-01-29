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
package eu.albina.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bedatadriven.jackson.datatype.jts.serialization.GeometryDeserializer;
import com.bedatadriven.jackson.datatype.jts.serialization.GeometrySerializer;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

import eu.albina.util.GlobalVariables;

/**
 * This class holds all information about one region.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "regions")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Region.class)
public class Region implements AvalancheInformationObject {

	@Id
	@Column(name = "ID")
	private String id;

	@Version
	@Column(name = "VERSION")
	private Integer version;

	@Column(name = "NAME_DE")
	private String nameDe;

	@Column(name = "NAME_IT")
	private String nameIt;

	@Column(name = "NAME_EN")
	private String nameEn;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(contentUsing = GeometryDeserializer.class)
	@Column(name = "POLYGON")
	private Polygon polygon;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "PARENTREGION_ID")
	private Region parentRegion;

	@OneToMany(mappedBy = "parentRegion", fetch = FetchType.EAGER)
	private Set<Region> subregions;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "AGGREGATEDREGION_ID")
	private Region aggregatedRegion;

	/**
	 * Default constructor. Initializes all collections of the region.
	 */
	public Region() {
		subregions = new HashSet<Region>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getNameDe() {
		return nameDe;
	}

	public void setNameDe(String name) {
		this.nameDe = name;
	}

	public String getNameIt() {
		return nameIt;
	}

	public void setNameIt(String name) {
		this.nameIt = name;
	}

	public String getNameEn() {
		return nameEn;
	}

	public void setNameEn(String name) {
		this.nameEn = name;
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

	public Region getAggregatedRegion() {
		return aggregatedRegion;
	}

	public void setAggregatedRegion(Region aggregatedRegion) {
		this.aggregatedRegion = aggregatedRegion;
	}

	public Set<Region> getSubregions() {
		return subregions;
	}

	public void setSubregions(Set<Region> subregions) {
		this.subregions = subregions;
	}

	public Element toCAAML(Document doc) {
		Element region = doc.createElement("Region");
		region.setAttribute("gml:id", getId());
		Element regionNameDe = doc.createElement("nameDe");
		regionNameDe.appendChild(doc.createTextNode(nameDe));
		Element regionNameIt = doc.createElement("nameIt");
		regionNameIt.appendChild(doc.createTextNode(nameIt));
		Element regionNameEn = doc.createElement("nameEn");
		regionNameEn.appendChild(doc.createTextNode(nameEn));
		region.appendChild(regionNameDe);
		Element regionSubType = doc.createElement("regionSubType");
		region.appendChild(regionSubType);
		Element outline = doc.createElement("outline");
		Element polygon = doc.createElement("gml:Polygon");
		polygon.setAttribute("gml:id", getId());
		polygon.setAttribute("srsDimension", "2");
		polygon.setAttribute("srsName", GlobalVariables.referenceSystemUrn);
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
		featureProperties.put("nameDe", nameDe);
		featureProperties.put("nameIt", nameIt);
		featureProperties.put("nameEn", nameEn);
		featureProperties.put("id", getId());
		if (getParentRegion() != null)
			featureProperties.put("parentRegion", getParentRegion().getId());
		if (getAggregatedRegion() != null)
			featureProperties.put("aggregatedRegion", getAggregatedRegion().getId());
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
