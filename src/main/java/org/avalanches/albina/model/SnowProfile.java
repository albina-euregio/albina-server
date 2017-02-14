package org.avalanches.albina.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class holds all information about one snow profile.
 * 
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "SNOW_PROFILES")
public class SnowProfile extends AbstractPersistentObject implements AvalancheInformationObject {

	/** Information about the author of the snow profile */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "AUTHOR_ID")
	private Author author;

	/** Information about the location of the snow profile */
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "country", column = @Column(name = "LOCATION_COUNTRY")),
			@AttributeOverride(name = "region", column = @Column(name = "LOCATION_REGION")),
			@AttributeOverride(name = "subregion", column = @Column(name = "LOCATION_SUBREGION")),
			@AttributeOverride(name = "name", column = @Column(name = "LOCATION_NAME")),
			@AttributeOverride(name = "geo.latitude", column = @Column(name = "LOCATION_LATITUDE")),
			@AttributeOverride(name = "geo.longitude", column = @Column(name = "LOCATION_LONGITUDE")),
			@AttributeOverride(name = "elevation", column = @Column(name = "LOCATION_ELEVATION")),
			@AttributeOverride(name = "angle", column = @Column(name = "LOCATION_ANGLE")),
			@AttributeOverride(name = "aspect", column = @Column(name = "LOCATION_ASPECT")),
			@AttributeOverride(name = "quality", column = @Column(name = "LOCATION_QUALITY")) })
	private Location location;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "dateTime", column = @Column(name = "DATETIME_DATETIME")),
			@AttributeOverride(name = "quality", column = @Column(name = "DATETIME_QUALITY")) })
	private DateTime dateTime;

	@Embedded
	private Conditions conditions;

	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "LAYERS", joinColumns = @JoinColumn(name = "SNOW_PROFILE_ID"))
	private Set<Layer> layers;

	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "STABILITY_TESTS", joinColumns = @JoinColumn(name = "SNOW_PROFILE_ID"))
	private Set<StabilityTest> stabilityTests;

	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "TEMPERATURE_MEASUREMENTS", joinColumns = @JoinColumn(name = "SNOW_PROFILE_ID"))
	private Set<TemperatureMeasurement> temperatureProfile;

	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "RAMM_MEASUREMENTS", joinColumns = @JoinColumn(name = "SNOW_PROFILE_ID"))
	private Set<RamMeasurement> rammProfile;

	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "DENSITY_MEASUREMENTS", joinColumns = @JoinColumn(name = "SNOW_PROFILE_ID"))
	private Set<DensityMeasurement> densityProfile;

	@OneToOne
	@JoinColumn(name = "COMMENT_ID")
	private Texts comment;

	/** String in JSON format to store additional information. */
	@Lob
	@Column(name = "CUSTOM_DATA")
	private String customData;

	/**
	 * Default constructor. Initializes all collections of the snow profile.
	 */
	public SnowProfile() {
		layers = new HashSet<Layer>();
		stabilityTests = new HashSet<StabilityTest>();
		temperatureProfile = new HashSet<TemperatureMeasurement>();
		rammProfile = new HashSet<RamMeasurement>();
		densityProfile = new HashSet<DensityMeasurement>();
	}

	/**
	 * Custom constructor using a json object to initialize the snow profile.
	 * 
	 * @param json
	 *            JSONObject containing all information about the new snow
	 *            profile.
	 */
	public SnowProfile(JSONObject json) {
		this();

		if (json.has("author") && !json.isNull("author"))
			author = new Author(json.getJSONObject("author"));

		if (json.has("location") && !json.isNull("location"))
			location = new Location(json.getJSONObject("location"));

		if (json.has("datetime") && !json.isNull("datetime"))
			dateTime = new DateTime(json.getJSONObject("datetime"));

		if (json.has("conditions") && !json.isNull("conditions"))
			conditions = new Conditions(json.getJSONObject("conditions"));

		if (json.has("layers") && !json.isNull("layers")) {
			JSONArray layers = json.getJSONArray("layers");
			for (Object entry : layers) {
				Layer layer = new Layer((JSONObject) entry);
				this.layers.add(layer);
			}
		}

		if (json.has("stabilityTests") && !json.isNull("stabilityTests")) {
			JSONArray stabilityTests = json.getJSONArray("stabilityTests");
			for (Object entry : stabilityTests) {
				StabilityTest stabilityTest = new StabilityTest((JSONObject) entry);
				this.stabilityTests.add(stabilityTest);
			}
		}

		if (json.has("temperatureProfile") && !json.isNull("temperatureProfile")) {
			JSONArray temperatureProfile = json.getJSONArray("temperatureProfile");
			for (Object entry : temperatureProfile) {
				TemperatureMeasurement temperatureMeasurement = new TemperatureMeasurement((JSONObject) entry);
				this.temperatureProfile.add(temperatureMeasurement);
			}
		}

		if (json.has("rammProfile") && !json.isNull("rammProfile")) {
			JSONArray rammProfile = json.getJSONArray("rammProfile");
			for (Object entry : rammProfile) {
				RamMeasurement rammMeasurement = new RamMeasurement((JSONObject) entry);
				this.rammProfile.add(rammMeasurement);
			}
		}

		if (json.has("densityProfile") && !json.isNull("densityProfile")) {
			JSONArray densityProfile = json.getJSONArray("densityProfile");
			for (Object entry : densityProfile) {
				DensityMeasurement densityMeasurement = new DensityMeasurement((JSONObject) entry);
				this.densityProfile.add(densityMeasurement);
			}
		}

		if (json.has("comment") && !json.isNull("comment")) {
			this.comment = new Texts(json.getJSONArray("comment"));
		}

		if (json.has("customData") && !json.isNull("customData"))
			this.customData = json.getString("customData");
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime time) {
		this.dateTime = time;
	}

	public Conditions getConditions() {
		return conditions;
	}

	public void setConditions(Conditions conditions) {
		this.conditions = conditions;
	}

	public Set<Layer> getLayers() {
		return layers;
	}

	public void setLayers(Set<Layer> layers) {
		this.layers = layers;
	}

	public Set<StabilityTest> getStabilityTests() {
		return stabilityTests;
	}

	public void setStabilityTests(Set<StabilityTest> stabilityTests) {
		this.stabilityTests = stabilityTests;
	}

	public Set<TemperatureMeasurement> getTemperatureProfile() {
		return temperatureProfile;
	}

	public void setTemperatureProfile(Set<TemperatureMeasurement> temperatureProfile) {
		this.temperatureProfile = temperatureProfile;
	}

	public Set<RamMeasurement> getRammProfile() {
		return rammProfile;
	}

	public void setRammProfile(Set<RamMeasurement> rammProfile) {
		this.rammProfile = rammProfile;
	}

	public Set<DensityMeasurement> getDensityProfile() {
		return densityProfile;
	}

	public void setDensityProfile(Set<DensityMeasurement> densityProfile) {
		this.densityProfile = densityProfile;
	}

	public Texts getComment() {
		return comment;
	}

	public void setComment(Texts comment) {
		this.comment = comment;
	}

	public String getCustomData() {
		return customData;
	}

	public void setCustomData(String customData) {
		this.customData = customData;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (author != null)
			json.put("author", author.toJSON());

		if (location != null)
			json.put("location", location.toJSON());

		if (dateTime != null)
			json.put("datetime", dateTime.toJSON());

		if (conditions != null)
			json.put("conditions", conditions.toJSON());

		if (layers != null && layers.size() > 0) {
			JSONArray layers = new JSONArray();
			for (Layer layer : this.layers) {
				layers.put(layer.toJSON());
			}
			json.put("layers", layers);
		}

		if (stabilityTests != null && stabilityTests.size() > 0) {
			JSONArray stabilityTests = new JSONArray();
			for (StabilityTest stabilityTest : this.stabilityTests) {
				stabilityTests.put(stabilityTest.toJSON());
			}
			json.put("stabilityTests", stabilityTests);
		}

		if (temperatureProfile != null && temperatureProfile.size() > 0) {
			JSONArray temperatureProfile = new JSONArray();
			for (TemperatureMeasurement temperatureMeasurment : this.temperatureProfile) {
				temperatureProfile.put(temperatureMeasurment.toJSON());
			}
			json.put("temperatureProfile", temperatureProfile);
		}

		if (rammProfile != null && rammProfile.size() > 0) {
			JSONArray rammProfile = new JSONArray();
			for (RamMeasurement rammMeasurment : this.rammProfile) {
				rammProfile.put(rammMeasurment.toJSON());
			}
			json.put("rammProfile", rammProfile);
		}

		if (densityProfile != null && densityProfile.size() > 0) {
			JSONArray densityProfile = new JSONArray();
			for (DensityMeasurement densityMeasurment : this.densityProfile) {
				densityProfile.put(densityMeasurment.toJSON());
			}
			json.put("densityProfile", densityProfile);
		}

		if (comment != null) {
			JSONArray array = comment.toJSONArray();
			json.put("comment", array);
		}

		if (customData != null && customData != "") {
			json.put("customData", customData);
		}

		return json;
	}

	/**
	 * This method serializes the snow profile to JSON omitting all nested
	 * collections (which enables lazy loading from DB).
	 * 
	 * @return Returns a string in JSON format representing the snow profile
	 *         without nested collections.
	 */
	public JSONObject toSmallJSON() {
		JSONObject json = new JSONObject();

		if (author != null)
			json.put("author", author.toJSON());

		if (location != null)
			json.put("location", location.toJSON());

		if (dateTime != null)
			json.put("datetime", dateTime.toJSON());

		if (conditions != null)
			json.put("conditions", conditions.toJSON());

		return json;
	}

}
