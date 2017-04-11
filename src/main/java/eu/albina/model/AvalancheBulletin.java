package eu.albina.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.albina.controller.UserController;
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
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	private User user;

	/** Validity of the avalanche bulletin */
	@Column(name = "VALID_FROM")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime validFrom;
	@Column(name = "VALID_UNTIL")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime validUntil;

	/** The regions the avalanche bulletin is for. */
	@ElementCollection
	@CollectionTable(name = "AVALANCHE_BULLETIN_REGIONS", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID")
	private Set<String> regions;

	@Column(name = "ELEVATION")
	private int elevation;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "ABOVE_ID")
	private AvalancheBulletinElevationDescription above;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "BELOW_ID")
	private AvalancheBulletinElevationDescription below;

	/** Map containing all text parts available for a bulletin */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "AVALANCHE_BULLETIN_TEXTS", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"), inverseJoinColumns = @JoinColumn(name = "TEXTS_ID"))
	@MapKeyEnumerated(EnumType.STRING)
	@MapKeyColumn(name = "TEXT_TYPE")
	private Map<TextPart, Texts> textPartsMap;

	/**
	 * Standard constructor for an avalanche bulletin.
	 */
	public AvalancheBulletin() {
		textPartsMap = new HashMap<TextPart, Texts>();
		regions = new HashSet<String>();
	}

	/**
	 * Custom constructor that creates an avalanche bulletin object from JSON
	 * input.
	 * 
	 * @param json
	 *            JSONObject holding information about an avalanche bulletin.
	 */
	public AvalancheBulletin(JSONObject json, String username) {
		this();

		try {
			this.user = UserController.getInstance().getUser(username);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

		if (json.has("regions")) {
			JSONArray regions = json.getJSONArray("regions");
			for (Object entry : regions) {
				this.regions.add((String) entry);
			}
		}

		if (json.has("above"))
			this.above = new AvalancheBulletinElevationDescription(json.getJSONObject("above"));

		if (json.has("below"))
			this.below = new AvalancheBulletinElevationDescription(json.getJSONObject("below"));
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public Set<String> getRegions() {
		return regions;
	}

	public void setRegions(Set<String> regions) {
		this.regions = regions;
	}

	public AvalancheBulletinElevationDescription getAbove() {
		return above;
	}

	public AvalancheBulletinElevationDescription getBelow() {
		return below;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (user != null && user.getName() != null && user.getName() != "")
			json.put("user", user.getName());

		for (TextPart part : TextPart.values()) {
			if ((textPartsMap.get(part) != null)) {
				json.put(part.toString(), textPartsMap.get(part).toJSONArray());
			}
		}

		JSONObject validity = new JSONObject();
		validity.put("from", validFrom.toString(GlobalVariables.formatterDateTime));
		validity.put("until", validUntil.toString(GlobalVariables.formatterDateTime));
		json.put("validity", validity);

		json.put("regions", regions);
		JSONArray regions = new JSONArray();
		for (Object region : regions) {
			regions.put(region);
		}

		json.put("above", above.toJSON());

		json.put("below", below.toJSON());

		return json;
	}

}
