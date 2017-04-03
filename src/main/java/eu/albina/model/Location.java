package eu.albina.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.json.JSONObject;

import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.CountryCode;
import eu.albina.model.enumerations.Quality;

@Embeddable
public class Location implements AvalancheInformationObject {

	@Column(name = "COUNTRY")
	@Enumerated(EnumType.STRING)
	private CountryCode countryCode;

	@Column(name = "REGION")
	private String region;

	@Column(name = "SUBREGION")
	private String subregion;

	@Column(name = "NAME")
	private String name;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "latitude", column = @Column(name = "LOCATION_LATITUDE")),
			@AttributeOverride(name = "longitude", column = @Column(name = "LOCATION_LONGITUDE")) })
	private Geo geo;

	@Column(name = "ELEVATION")
	private int elevation;

	@Column(name = "ANGLE")
	private int angle;

	@Column(name = "ASPECT")
	@Enumerated(EnumType.STRING)
	private Aspect aspect;

	@Column(name = "QUALITY")
	@Enumerated(EnumType.STRING)
	private Quality quality;

	public Location() {
		elevation = -1;
		angle = -1;
	}

	public Location(JSONObject json) {
		this();

		if (json.has("country") && !json.isNull("country"))
			this.countryCode = CountryCode.valueOf(json.getString("country").toUpperCase());
		if (json.has("region") && !json.isNull("region"))
			this.region = json.getString("region");
		if (json.has("subregion") && !json.isNull("subregion"))
			this.subregion = json.getString("subregion");
		if (json.has("name") && !json.isNull("name"))
			this.name = json.getString("name");
		if (json.has("geo") && !json.isNull("geo"))
			this.geo = new Geo(json.getJSONObject("geo"));
		if (json.has("elevation") && !json.isNull("elevation"))
			this.elevation = json.getInt("elevation");
		if (json.has("angle") && !json.isNull("angle"))
			this.angle = json.getInt("angle");
		if (json.has("aspect") && !json.isNull("aspect")) {
			// TODO delete (just for testing)
			// this.aspect = json.getString("aspect");
			Object object = json.get("aspect");
			if (object instanceof String) {
				String a = json.getString("aspect").toUpperCase();
				if (a.equals("SO"))
					a = "SE";
				else if (a.equals("O"))
					a = "E";
				else if (a.equals("NO"))
					a = "NE";
				this.aspect = Aspect.valueOf(a);
			} else {
				int a = json.getInt("aspect");
				switch (a) {
				case 1:
					this.aspect = Aspect.N;
					break;
				case 2:
					this.aspect = Aspect.NE;
					break;
				case 3:
					this.aspect = Aspect.E;
					break;
				case 4:
					this.aspect = Aspect.SE;
					break;
				case 5:
					this.aspect = Aspect.S;
					break;
				case 6:
					this.aspect = Aspect.SW;
					break;
				case 7:
					this.aspect = Aspect.W;
					break;
				case 8:
					this.aspect = Aspect.NW;
					break;
				default:
					break;
				}
			}
		}
		if (json.has("quality") && !json.isNull("quality"))
			this.quality = Quality.valueOf(json.getString("quality").toLowerCase());
	}

	public CountryCode getCountry() {
		return countryCode;
	}

	public void setCountry(CountryCode country) {
		this.countryCode = country;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getSubregion() {
		return subregion;
	}

	public void setSubregion(String subregion) {
		this.subregion = subregion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Geo getGeo() {
		return geo;
	}

	public void setGeo(Geo geo) {
		this.geo = geo;
	}

	public int getElevation() {
		return elevation;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public Aspect getAspect() {
		return aspect;
	}

	public void setAspect(Aspect aspect) {
		this.aspect = aspect;
	}

	public Quality getQuality() {
		return quality;
	}

	public void setQuality(Quality quality) {
		this.quality = quality;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (countryCode != null)
			json.put("country", countryCode.toString());
		if (region != null && region != "")
			json.put("region", region);
		if (subregion != null && subregion != "")
			json.put("subregion", subregion);
		if (name != null && name != "")
			json.put("name", name);
		if (geo != null)
			json.put("geo", geo.toJSON());
		if (elevation > -1)
			json.put("elevation", elevation);
		if (angle > -1)
			json.put("angle", angle);
		if (aspect != null)
			json.put("aspect", aspect.toString());
		if (quality != null)
			json.put("quality", quality.toString());

		return json;
	}

}
