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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.common.io.Resources;
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

	public Region(JSONObject object) {
		this();
		if (!"Feature".equals(object.getString("type"))) {
			throw new IllegalArgumentException("Expecting type=Feature");
		}
		final JSONObject properties = object.getJSONObject("properties");
		nameDe = properties.getString("nameDe");
		nameIt = properties.getString("nameIt");
		nameEn = properties.getString("nameEn");
		id = properties.getString("id");

		try {
			polygon = new ObjectMapper().registerModule(new JtsModule())
				.readValue(object.getJSONObject("geometry").toString(), Polygon.class);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Region region = (Region) o;
		return Objects.equals(id, region.id) &&
			Objects.equals(version, region.version) &&
			Objects.equals(nameDe, region.nameDe) &&
			Objects.equals(nameIt, region.nameIt) &&
			Objects.equals(nameEn, region.nameEn) &&
			Objects.equals(polygon, region.polygon);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, version, nameDe, nameIt, nameEn, polygon);
	}

	public static Region readRegion(final URL resource) throws IOException {
		final String string = Resources.toString(resource, StandardCharsets.UTF_8);
		return new Region(new JSONObject(string));
	}

	public static List<Region> readRegions(final URL resource) throws IOException {
		final String string = Resources.toString(resource, StandardCharsets.UTF_8);
		final JSONObject object = new JSONObject(string);
		if (!"FeatureCollection".equals(object.getString("type"))) {
			throw new IllegalArgumentException("Expecting type=FeatureCollection");
		}
		final JSONArray features = object.getJSONArray("features");
		return IntStream.range(0, features.length()).mapToObj(features::getJSONObject).map(Region::new).collect(Collectors.toList());
	}
}
