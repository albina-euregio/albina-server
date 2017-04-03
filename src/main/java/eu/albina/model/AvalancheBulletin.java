package eu.albina.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.albina.model.enumerations.CountryCode;
import eu.albina.model.enumerations.Tendency;
import eu.albina.model.enumerations.TextPart;
import eu.albina.util.GlobalVariables;

/**
 * This class holds all information about one avalanche bulletin.
 * 
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "AVALANCHE_BULLETINS")
public class AvalancheBulletin extends AbstractPersistentObject implements AvalancheInformationObject {

	/** Information about the author of the avalanche bulletin */
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AUTHOR_ID")
	private Author author;

	/** Tendency of the danger rating for the next day */
	@Column(name = "TENDENCY")
	@Enumerated(EnumType.STRING)
	private Tendency tendency;

	/** Map containing all text parts available for a bulletin */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "AVALANCHE_BULLETIN_TEXTS", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"), inverseJoinColumns = @JoinColumn(name = "TEXTS_ID"))
	@MapKeyEnumerated(EnumType.STRING)
	@MapKeyColumn(name = "TEXT_TYPE")
	private Map<TextPart, Texts> textPartsMap;

	/** Validity of the avalanche bulletin */
	@Column(name = "VALID_FROM")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime validFrom;
	@Column(name = "VALID_UNTIL")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime validUntil;

	/** The country the avalanche bulletin is for. */
	@Column(name = "COUNTRY")
	@Enumerated(EnumType.STRING)
	private CountryCode country;

	/** The region the avalanche bulletin is for. */
	@Column(name = "REGION")
	private String region;

	/**
	 * Specific information on the avalanche situation for individual time
	 * intervals.
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "AVALANCHE_BULLETIN_DAYTIME_BULLETINS", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"), inverseJoinColumns = @JoinColumn(name = "DAYTIME_BULLETIN_ID"))
	private Set<DaytimeBulletin> daytimeBulletins;

	/** String in JSON format to store additional information. */
	@Lob
	@Column(name = "CUSTOM_DATA")
	private String customData;

	/**
	 * Standard constructor for an avalanche bulletin.
	 */
	public AvalancheBulletin() {
		textPartsMap = new HashMap<TextPart, Texts>();
		daytimeBulletins = new HashSet<DaytimeBulletin>();
	}

	/**
	 * Custom constructor that creates an avalanche bulletin object from JSON
	 * input.
	 * 
	 * @param json
	 *            JSONObject holding information about an avalanche bulletin.
	 */
	public AvalancheBulletin(JSONObject json) {
		this();

		if (json.has("author"))
			this.author = new Author((JSONObject) json.get("author"));

		if (json.has("tendency"))
			this.tendency = Tendency.valueOf((json.getString("tendency")).toLowerCase());

		for (TextPart part : TextPart.values()) {
			if (json.has(part.toString())) {
				this.textPartsMap.put(part, new Texts(json.getJSONArray(part.toString())));
			}
		}

		if (json.has("validity")) {
			JSONObject validity = json.getJSONObject("validity");
			this.validFrom = new org.joda.time.DateTime(validity.getString("from"));
			this.validUntil = new org.joda.time.DateTime(validity.getString("until"));
		}

		if (json.has("country"))
			this.country = CountryCode.valueOf((json.getString("country")).toUpperCase());

		if (json.has("region"))
			this.region = json.getString("region");

		if (json.has("daytimeBulletins")) {
			JSONArray daytimeBulletins = json.getJSONArray("daytimeBulletins");
			for (Object entry : daytimeBulletins) {
				this.daytimeBulletins.add(new DaytimeBulletin((JSONObject) entry));
			}
		}

		if (json.has("customData"))
			this.customData = json.getString("customData");
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public Tendency getTendency() {
		return tendency;
	}

	public void setTendency(Tendency tendency) {
		this.tendency = tendency;
	}

	public Texts getAvalancheSituationHighlight() {
		return textPartsMap.get(TextPart.avalancheSituationHighlight);
	}

	public void setAvalancheSituationHighlight(Texts avalancheSituationHighlight) {
		textPartsMap.put(TextPart.avalancheSituationHighlight, avalancheSituationHighlight);
	}

	public Texts getAvalancheSituationComment() {
		return textPartsMap.get(TextPart.avalancheSituationComment);
	}

	public void setAvalancheSituationComment(Texts avalancheSituationComment) {
		textPartsMap.put(TextPart.avalancheSituationComment, avalancheSituationComment);
	}

	public Texts getActivityHighlight() {
		return textPartsMap.get(TextPart.activityHighlight);
	}

	public void setActivityHighlight(Texts activityHighlight) {
		textPartsMap.put(TextPart.activityHighlight, activityHighlight);
	}

	public Texts getActivityComment() {
		return textPartsMap.get(TextPart.activityComment);
	}

	public void setActivityComment(Texts activityComment) {
		textPartsMap.put(TextPart.activityComment, activityComment);
	}

	public Texts getSynopsisHighlight() {
		return textPartsMap.get(TextPart.synopsisHighlight);
	}

	public void setSynopsisHighlight(Texts synopsisHighlight) {
		textPartsMap.put(TextPart.synopsisHighlight, synopsisHighlight);
	}

	public Texts getSynopsisComment() {
		return textPartsMap.get(TextPart.synopsisComment);
	}

	public void setSynopsisComment(Texts synopsisComment) {
		textPartsMap.put(TextPart.synopsisComment, synopsisComment);
	}

	public Texts getSnowpackStructureHighlight() {
		return textPartsMap.get(TextPart.snowpackStructureHighlight);
	}

	public void setSnowpackStructureHighlight(Texts snowpackStructureHighlight) {
		textPartsMap.put(TextPart.snowpackStructureHighlight, snowpackStructureHighlight);
	}

	public Texts getSnowpackStructureComment() {
		return textPartsMap.get(TextPart.snowpackStructureComment);
	}

	public void setSnowpackStructureComment(Texts snowpackStructureComment) {
		textPartsMap.put(TextPart.snowpackStructureComment, snowpackStructureComment);
	}

	public Texts getTravelAdvisoryHighlight() {
		return textPartsMap.get(TextPart.travelAdvisoryHighlight);
	}

	public void setTravelAdvisoryHighlight(Texts travelAdvisoryHighlight) {
		textPartsMap.put(TextPart.travelAdvisoryHighlight, travelAdvisoryHighlight);
	}

	public Texts getTravelAdvisoryComment() {
		return textPartsMap.get(TextPart.travelAdvisoryComment);
	}

	public void setTravelAdvisoryComment(Texts travelAdvisoryComment) {
		textPartsMap.put(TextPart.travelAdvisoryComment, travelAdvisoryComment);
	}

	public Texts getTendencyComment() {
		return textPartsMap.get(TextPart.tendencyComment);
	}

	public void setTendencyComment(Texts tendencyComment) {
		textPartsMap.put(TextPart.tendencyComment, tendencyComment);
	}

	public org.joda.time.DateTime getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(org.joda.time.DateTime validFrom) {
		this.validFrom = validFrom;
	}

	public org.joda.time.DateTime getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(org.joda.time.DateTime validUntil) {
		this.validUntil = validUntil;
	}

	public CountryCode getCountry() {
		return country;
	}

	public void setCountry(CountryCode country) {
		this.country = country;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public Set<DaytimeBulletin> getDaytimeBulletins() {
		return daytimeBulletins;
	}

	public void setDaytimeBulletins(Set<DaytimeBulletin> daytimeBulletins) {
		this.daytimeBulletins = daytimeBulletins;
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

		if (tendency != null)
			json.put("tendency", tendency.toString());

		for (TextPart part : TextPart.values()) {
			if ((textPartsMap.get(part) != null)) {
				json.put(part.toString(), textPartsMap.get(part).toJSONArray());
			}
		}

		JSONObject validity = new JSONObject();
		validity.put("from", validFrom.toString(GlobalVariables.formatterDateTime));
		validity.put("until", validUntil.toString(GlobalVariables.formatterDateTime));
		json.put("validity", validity);

		json.put("country", country.toString());
		json.put("region", region);

		JSONArray bulletins = new JSONArray();
		for (DaytimeBulletin daytimeBulletin : daytimeBulletins) {
			bulletins.put(daytimeBulletin.toJSON());
		}
		json.put("daytimeBulletins", bulletins);

		json.put("customData", customData);

		return json;
	}

}
