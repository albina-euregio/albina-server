package eu.albina.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.hibernate.envers.Audited;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.controller.UserController;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.TextPart;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.AuthorizationUtil;
import eu.albina.util.GlobalVariables;

/**
 * This class holds all information about one avalanche bulletin.
 * 
 * @author Norbert Lanzanasto
 *
 */
@Audited
@Entity
@Table(name = "AVALANCHE_BULLETINS")
public class AvalancheBulletin extends AbstractPersistentObject implements AvalancheInformationObject {

	/** Information about the author of the avalanche bulletin */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	private User user;

	@Column(name = "CREATOR")
	private String creator;
	@Column(name = "CREATOR_REGION")
	private String creatorRegion;

	@Column(name = "PUBLICATION_DATE")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime publicationDate;

	/** Validity of the avalanche bulletin */
	@Column(name = "VALID_FROM")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime validFrom;
	@Column(name = "VALID_UNTIL")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime validUntil;

	/** The recommended regions the avalanche bulletin is for. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "AVALANCHE_BULLETIN_SUGGESTED_REGIONS", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID")
	private Set<String> suggestedRegions;

	/** The published regions the avalanche bulletin is for. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "AVALANCHE_BULLETIN_PUBLISHED_REGIONS", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID")
	private Set<String> publishedRegions;

	/** The saved regions the avalanche bulletin is for. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "AVALANCHE_BULLETIN_SAVED_REGIONS", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID")
	private Set<String> savedRegions;

	@Column(name = "ELEVATION")
	private int elevation;

	@Column(name = "TREELINE")
	private boolean treeline;

	@Column(name = "HAS_DAYTIME_DEPENDENCY")
	private boolean hasDaytimeDependency;

	@Column(name = "HAS_ELEVATION_DEPENDENCY")
	private boolean hasElevationDependency;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "FORENOON_ABOVE_ID")
	private AvalancheBulletinElevationDescription forenoonAbove;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "FORENOON_BELOW_ID")
	private AvalancheBulletinElevationDescription forenoonBelow;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AFTERNOON_ABOVE_ID")
	private AvalancheBulletinElevationDescription afternoonAbove;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AFTERNOON_BELOW_ID")
	private AvalancheBulletinElevationDescription afternoonBelow;

	@Column(name = "AV_ACTIVITY_HIGHLIGHTS_TEXTCAT")
	private String avActivityHighlightsTextcat;

	@Column(name = "AV_ACTIVITY_COMMENT_TEXTCAT")
	private String avActivityCommentTextcat;

	@Column(name = "SNOWPACK_STRUCTURE_HIGHLIGHTS_TEXTCAT")
	private String snowpackStructureHighlightsTextcat;

	@Column(name = "SNOWPACK_STRUCTURE_COMMENT_TEXTCAT")
	private String snowpackStructureCommentTextcat;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_PATTERN_1")
	private DangerPattern dangerPattern1;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_PATTERN_2")
	private DangerPattern dangerPattern2;

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
		publishedRegions = new HashSet<String>();
		savedRegions = new HashSet<String>();
		suggestedRegions = new HashSet<String>();
	}

	/**
	 * Custom constructor that creates an avalanche bulletin object from JSON input.
	 * 
	 * @param json
	 *            JSONObject holding information about an avalanche bulletin.
	 */
	public AvalancheBulletin(JSONObject json, String username) {
		this();

		if (json.has("id"))
			this.id = json.getString("id");

		if (username != null) {
			try {
				this.user = UserController.getInstance().getUser(username);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (json.has("creator"))
			this.creator = json.getString("creator");
		if (json.has("creatorRegion"))
			this.creatorRegion = json.getString("creatorRegion");

		if (json.has("avActivityHighlightsTextcat"))
			this.avActivityHighlightsTextcat = json.getString("avActivityHighlightsTextcat");
		if (json.has("avActivityCommentTextcat"))
			this.avActivityCommentTextcat = json.getString("avActivityCommentTextcat");
		if (json.has("snowpackStructureHighlightsTextcat"))
			this.snowpackStructureHighlightsTextcat = json.getString("snowpackStructureHighlightsTextcat");
		if (json.has("snowpackStructureCommentTextcat"))
			this.snowpackStructureCommentTextcat = json.getString("snowpackStructureCommentTextcat");

		if (json.has("dangerPattern1"))
			this.dangerPattern1 = DangerPattern.valueOf(json.getString("dangerPattern1").toLowerCase());
		if (json.has("dangerPattern2"))
			this.dangerPattern2 = DangerPattern.valueOf(json.getString("dangerPattern2").toLowerCase());

		for (TextPart part : TextPart.values()) {
			if (json.has(part.toString())) {
				this.textPartsMap.put(part, new Texts(json.getJSONArray(part.toString())));
			}
		}

		if (json.has("publicationDate"))
			this.publicationDate = new org.joda.time.DateTime(json.getString("publicationDate"));

		if (json.has("validity")) {
			JSONObject validity = json.getJSONObject("validity");
			this.validFrom = new org.joda.time.DateTime(validity.getString("from"));
			this.validUntil = new org.joda.time.DateTime(validity.getString("until"));
		}

		if (json.has("suggestedRegions")) {
			JSONArray regions = json.getJSONArray("suggestedRegions");
			for (Object entry : regions) {
				this.suggestedRegions.add((String) entry);
			}
		}

		if (json.has("publishedRegions")) {
			JSONArray regions = json.getJSONArray("publishedRegions");
			for (Object entry : regions) {
				this.publishedRegions.add((String) entry);
			}
		}

		if (json.has("savedRegions")) {
			JSONArray regions = json.getJSONArray("savedRegions");
			for (Object entry : regions) {
				this.savedRegions.add((String) entry);
			}
		}

		if (json.has("hasDaytimeDependency"))
			this.hasDaytimeDependency = json.getBoolean("hasDaytimeDependency");

		if (json.has("hasElevationDependency"))
			this.hasElevationDependency = json.getBoolean("hasElevationDependency");

		if (json.has("elevation"))
			this.elevation = json.getInt("elevation");

		if (json.has("treeline"))
			this.treeline = json.getBoolean("treeline");

		if (json.has("forenoonAbove"))
			this.forenoonAbove = new AvalancheBulletinElevationDescription(json.getJSONObject("forenoonAbove"));

		if (json.has("forenoonBelow"))
			this.forenoonBelow = new AvalancheBulletinElevationDescription(json.getJSONObject("forenoonBelow"));

		if (json.has("afternoonAbove"))
			this.afternoonAbove = new AvalancheBulletinElevationDescription(json.getJSONObject("afternoonAbove"));

		if (json.has("afternoonBelow"))
			this.afternoonBelow = new AvalancheBulletinElevationDescription(json.getJSONObject("afternoonBelow"));
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreatorRegion() {
		return creatorRegion;
	}

	public void setCreatorRegion(String creatorRegion) {
		this.creatorRegion = creatorRegion;
	}

	public String getAvActivityHighlightsTextcat() {
		return avActivityHighlightsTextcat;
	}

	public void setAvActivityHighlightsTextcat(String avActivityHighlightsTextcat) {
		this.avActivityHighlightsTextcat = avActivityHighlightsTextcat;
	}

	public String getAvActivityCommentTextcat() {
		return avActivityCommentTextcat;
	}

	public void setAvActivityCommentTextcat(String avActivityCommentTextcat) {
		this.avActivityCommentTextcat = avActivityCommentTextcat;
	}

	public String getSnowpackStructureHighlightsTextcat() {
		return snowpackStructureHighlightsTextcat;
	}

	public void setSnowpackStructureHighlightsTextcat(String snowpackStructureHighlightsTextcat) {
		this.snowpackStructureHighlightsTextcat = snowpackStructureHighlightsTextcat;
	}

	public String getSnowpackStructureCommentTextcat() {
		return snowpackStructureCommentTextcat;
	}

	public void setSnowpackStructureCommentTextcat(String snowpackStructureCommentTextcat) {
		this.snowpackStructureCommentTextcat = snowpackStructureCommentTextcat;
	}

	public Texts getAvActivityHighlights() {
		return textPartsMap.get(TextPart.avActivityHighlights);
	}

	public void setAvActivityHighlights(Texts avActivityHighlights) {
		textPartsMap.put(TextPart.avActivityHighlights, avActivityHighlights);
	}

	public Texts getAvActivityComment() {
		return textPartsMap.get(TextPart.avActivityComment);
	}

	public void setAvActivityComment(Texts avActivityComment) {
		textPartsMap.put(TextPart.avActivityComment, avActivityComment);
	}

	public Texts getSynopsisHighlights() {
		return textPartsMap.get(TextPart.synopsisHighlights);
	}

	public void setSynopsisHighlights(Texts synopsisHighlights) {
		textPartsMap.put(TextPart.synopsisHighlights, synopsisHighlights);
	}

	public Texts getSynopsisComment() {
		return textPartsMap.get(TextPart.synopsisComment);
	}

	public void setSynopsisComment(Texts synopsisComment) {
		textPartsMap.put(TextPart.synopsisComment, synopsisComment);
	}

	public Texts getSnowpackStructureHighlights() {
		return textPartsMap.get(TextPart.snowpackStructureHighlights);
	}

	public void setSnowpackStructureHighlights(Texts snowpackStructureHighlights) {
		textPartsMap.put(TextPart.snowpackStructureHighlights, snowpackStructureHighlights);
	}

	public Texts getSnowpackStructureComment() {
		return textPartsMap.get(TextPart.snowpackStructureComment);
	}

	public void setSnowpackStructureComment(Texts snowpackStructureComment) {
		textPartsMap.put(TextPart.snowpackStructureComment, snowpackStructureComment);
	}

	public Texts getTravelAdvisoryHighlights() {
		return textPartsMap.get(TextPart.travelAdvisoryHighlights);
	}

	public void setTravelAdvisoryHighlights(Texts travelAdvisoryHighlights) {
		textPartsMap.put(TextPart.travelAdvisoryHighlights, travelAdvisoryHighlights);
	}

	public Texts getTravelAdvisoryComment() {
		return textPartsMap.get(TextPart.travelAdvisoryComment);
	}

	public void setTravelAdvisoryComment(Texts travelAdvisoryComment) {
		textPartsMap.put(TextPart.travelAdvisoryComment, travelAdvisoryComment);
	}

	public DangerPattern getDangerPattern1() {
		return dangerPattern1;
	}

	public void setDangerPattern1(DangerPattern dangerPattern) {
		this.dangerPattern1 = dangerPattern;
	}

	public DangerPattern getDangerPattern2() {
		return dangerPattern2;
	}

	public void setDangerPattern2(DangerPattern dangerPattern) {
		this.dangerPattern2 = dangerPattern;
	}

	public org.joda.time.DateTime getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(org.joda.time.DateTime publicationDate) {
		this.publicationDate = publicationDate;
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

	public Set<String> getSuggestedRegions() {
		return suggestedRegions;
	}

	public void setSuggestedRegions(Set<String> regions) {
		this.suggestedRegions = regions;
	}

	public Set<String> getSavedRegions() {
		return savedRegions;
	}

	public void setSavedRegions(Set<String> regions) {
		this.savedRegions = regions;
	}

	public Set<String> getPublishedRegions() {
		return publishedRegions;
	}

	public void setPublishedRegions(Set<String> regions) {
		this.publishedRegions = regions;
	}

	public AvalancheBulletinElevationDescription getForenoonAbove() {
		return forenoonAbove;
	}

	public void setForenoonAbove(AvalancheBulletinElevationDescription forenoonAbove) {
		this.forenoonAbove = forenoonAbove;
	}

	public AvalancheBulletinElevationDescription getForenoonBelow() {
		return forenoonBelow;
	}

	public void setForenoonBelow(AvalancheBulletinElevationDescription forenoonBelow) {
		this.forenoonBelow = forenoonBelow;
	}

	public AvalancheBulletinElevationDescription getAfternoonAbove() {
		return afternoonAbove;
	}

	public void setAfternoonAbove(AvalancheBulletinElevationDescription afternoonAbove) {
		this.afternoonAbove = afternoonAbove;
	}

	public AvalancheBulletinElevationDescription getAfternoonBelow() {
		return afternoonBelow;
	}

	public void setAfternoonBelow(AvalancheBulletinElevationDescription afternoonBelow) {
		this.afternoonBelow = afternoonBelow;
	}

	public BulletinStatus getStatus(List<String> regions) {
		BulletinStatus result = BulletinStatus.draft;
		for (String entry : getPublishedRegions())
			for (String region : regions)
				if (entry.startsWith(region))
					result = BulletinStatus.published;

		return result;
	}

	public BulletinStatus getStatus(String region) {
		BulletinStatus result = BulletinStatus.draft;
		for (String entry : getPublishedRegions())
			if (entry.startsWith(region))
				result = BulletinStatus.published;

		return result;
	}

	public int getElevation() {
		return elevation;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public boolean getTreeline() {
		return treeline;
	}

	public void setTreeline(boolean treeline) {
		this.treeline = treeline;
	}

	public boolean isHasDaytimeDependency() {
		return hasDaytimeDependency;
	}

	public void setHasDaytimeDependency(boolean hasDaytimeDependency) {
		this.hasDaytimeDependency = hasDaytimeDependency;
	}

	public boolean isHasElevationDependency() {
		return hasElevationDependency;
	}

	public void setHasElevationDependency(boolean hasElevationDependency) {
		this.hasElevationDependency = hasElevationDependency;
	}

	public boolean affectsRegion(String region) {
		if (getSuggestedRegions() != null)
			for (String entry : getSuggestedRegions())
				if (entry.startsWith(region))
					return true;
		if (getSavedRegions() != null)
			for (String entry : getSavedRegions())
				if (entry.startsWith(region))
					return true;
		if (getPublishedRegions() != null)
			for (String entry : getPublishedRegions())
				if (entry.startsWith(region))
					return true;

		return false;
	}

	public boolean hasDaytimeDependency() {
		Duration duration = new Duration(validFrom, validUntil);
		long standardHours = duration.getStandardHours();
		if (standardHours < 24)
			return true;
		else
			return false;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (id != null && id != "")
			json.put("id", id);

		if (user != null && user.getName() != null && user.getName() != "")
			json.put("user", user.getName());

		if (user != null && user.getRole() != null)
			json.put("ownerRegion", AuthorizationUtil.getRegion(user.getRole()));

		if (creator != null && creator != "")
			json.put("creator", creator);

		if (creatorRegion != null && creatorRegion != "")
			json.put("creatorRegion", creatorRegion);

		if (avActivityHighlightsTextcat != null && avActivityHighlightsTextcat != "")
			json.put("avActivityHighlightsTextcat", avActivityHighlightsTextcat);
		if (avActivityCommentTextcat != null && avActivityCommentTextcat != "")
			json.put("avActivityCommentTextcat", avActivityCommentTextcat);
		if (snowpackStructureHighlightsTextcat != null && snowpackStructureHighlightsTextcat != "")
			json.put("snowpackStructureHighlightsTextcat", snowpackStructureHighlightsTextcat);
		if (snowpackStructureCommentTextcat != null && snowpackStructureCommentTextcat != "")
			json.put("snowpackStructureCommentTextcat", snowpackStructureCommentTextcat);

		for (TextPart part : TextPart.values())
			if ((textPartsMap.get(part) != null))
				json.put(part.toString(), textPartsMap.get(part).toJSONArray());

		if (dangerPattern1 != null)
			json.put("dangerPattern1", this.dangerPattern1.toString());

		if (dangerPattern2 != null)
			json.put("dangerPattern2", this.dangerPattern2.toString());

		if (publicationDate != null)
			json.put("publicationDate", publicationDate.toString(GlobalVariables.formatterDateTime));

		JSONObject validity = new JSONObject();
		validity.put("from", validFrom.toString(GlobalVariables.formatterDateTime));
		validity.put("until", validUntil.toString(GlobalVariables.formatterDateTime));
		json.put("validity", validity);

		json.put("suggestedRegions", suggestedRegions);
		json.put("savedRegions", savedRegions);
		json.put("publishedRegions", publishedRegions);

		json.put("hasDaytimeDependency", hasDaytimeDependency);
		json.put("hasElevationDependency", hasElevationDependency);

		if (hasElevationDependency) {
			if (treeline) {
				json.put("treeline", treeline);
			} else {
				json.put("elevation", elevation);
			}
		}

		if (hasElevationDependency && forenoonBelow != null)
			json.put("forenoonBelow", forenoonBelow.toJSON());

		if (forenoonAbove != null)
			json.put("forenoonAbove", forenoonAbove.toJSON());

		if (hasDaytimeDependency && afternoonAbove != null)
			json.put("afternoonAbove", afternoonAbove.toJSON());

		if (hasDaytimeDependency && hasElevationDependency && afternoonBelow != null)
			json.put("afternoonBelow", afternoonBelow.toJSON());

		return json;
	}

	private Element createCAAMLBulletin(Document doc, LanguageCode languageCode, DateTime startDate, int version,
			boolean isAfternoon) {

		// TODO add danger patterns to CAAML

		AvalancheBulletinElevationDescription above;
		AvalancheBulletinElevationDescription below;

		if (isAfternoon) {
			above = this.afternoonAbove;
			below = this.afternoonBelow;
		} else {
			above = this.forenoonAbove;
			below = this.forenoonBelow;
		}

		Element rootElement = doc.createElement("Bulletin");
		if (getId() != null)
			rootElement.setAttribute("gml:id", getId());
		if (languageCode == null)
			languageCode = LanguageCode.en;
		rootElement.setAttribute("xml:lang", languageCode.toString());

		Element metaDataProperty = doc.createElement("metaDataProperty");
		Element metaData = doc.createElement("MetaData");
		Element dateTimeReport = doc.createElement("dateTimeReport");
		dateTimeReport.appendChild(doc.createTextNode(publicationDate.toString(GlobalVariables.formatterDateTime)));
		metaData.appendChild(dateTimeReport);
		if (user != null) {
			Element srcRef = doc.createElement("srcRef");
			srcRef.appendChild(user.toCAAML(doc));
			metaData.appendChild(srcRef);
		}

		Element resolution300 = doc.createElement("albina:resolution");
		resolution300.appendChild(doc.createTextNode("300"));
		Element filetypeJPG = doc.createElement("albina:filetype");
		filetypeJPG.appendChild(doc.createTextNode(GlobalVariables.fileExtensionJpg));

		Element customData = doc.createElement("customData");
		Element dangerRatingMap = doc.createElement("albina:DangerRatingMap");
		dangerRatingMap.appendChild(resolution300);
		dangerRatingMap.appendChild(filetypeJPG);
		Element url = doc.createElement("albina:url");
		url.appendChild(doc.createTextNode(AlbinaUtil.createMapUrlAggregatedRegion(startDate, version, getId(),
				GlobalVariables.fileExtensionJpg)));
		dangerRatingMap.appendChild(url);
		customData.appendChild(dangerRatingMap);
		metaData.appendChild(customData);

		metaDataProperty.appendChild(metaData);
		rootElement.appendChild(metaDataProperty);

		for (String region : publishedRegions) {
			Element locRef = doc.createElement("locRef");
			locRef.setAttribute("xlink:href", region);
			rootElement.appendChild(locRef);
		}

		if (validFrom != null && validUntil != null) {

			DateTime start = new DateTime(validFrom);
			DateTime end = new DateTime(validUntil);

			if (hasDaytimeDependency) {
				DateTime noon = new DateTime(validFrom);
				noon = noon.withHourOfDay(12);

				if (noon.isBefore(validFrom))
					noon = noon.plusDays(1);

				if (!isAfternoon)
					end = new DateTime(noon);
				else if (isAfternoon)
					start = new DateTime(noon);
			}

			Element validTime = doc.createElement("validTime");
			Element timePeriod = doc.createElement("TimePeriod");
			Element beginPosition = doc.createElement("beginPosition");
			beginPosition.appendChild(doc.createTextNode(start.toString(GlobalVariables.formatterDateTime)));
			timePeriod.appendChild(beginPosition);
			Element endPosition = doc.createElement("endPosition");
			endPosition.appendChild(doc.createTextNode(end.toString(GlobalVariables.formatterDateTime)));
			timePeriod.appendChild(endPosition);
			validTime.appendChild(timePeriod);
			rootElement.appendChild(validTime);
		}

		Element bulletinResultsOf = doc.createElement("bulletinResultsOf");
		Element bulletinMeasurements = doc.createElement("BulletinMeasurements");

		Element dangerRatings = doc.createElement("dangerRatings");
		if (hasElevationDependency) {
			Element dangerRatingAbove = doc.createElement("DangerRating");
			Element validElevationAbove = doc.createElement("validElevation");
			validElevationAbove.setAttribute("xlink:href",
					AlbinaUtil.createValidElevationAttribute(elevation, true, treeline));
			dangerRatingAbove.appendChild(validElevationAbove);
			if (above != null && above.getDangerRating() != null) {
				Element mainValueAbove = doc.createElement("mainValue");
				mainValueAbove.appendChild(doc.createTextNode(DangerRating.getCAAMLString(above.getDangerRating())));
				dangerRatingAbove.appendChild(mainValueAbove);
			}
			dangerRatings.appendChild(dangerRatingAbove);
			Element dangerRatingBelow = doc.createElement("DangerRating");
			Element validElevationBelow = doc.createElement("validElevation");
			validElevationBelow.setAttribute("xlink:href",
					AlbinaUtil.createValidElevationAttribute(elevation, false, treeline));
			dangerRatingBelow.appendChild(validElevationBelow);
			if (below != null && below.getDangerRating() != null) {
				Element mainValueBelow = doc.createElement("mainValue");
				// TODO add treeline as elevation
				mainValueBelow.appendChild(doc.createTextNode(DangerRating.getCAAMLString(below.getDangerRating())));
				dangerRatingBelow.appendChild(mainValueBelow);
			}
			dangerRatings.appendChild(dangerRatingBelow);
		} else {
			// NOTE if no elevation dependency is set, the elevation description is above
			Element dangerRating = doc.createElement("DangerRating");
			if (above != null && above.getDangerRating() != null) {
				Element mainValue = doc.createElement("mainValue");
				mainValue.appendChild(doc.createTextNode(DangerRating.getCAAMLString(above.getDangerRating())));
				dangerRating.appendChild(mainValue);
			}
			dangerRatings.appendChild(dangerRating);
		}
		bulletinMeasurements.appendChild(dangerRatings);

		Element avProblems = doc.createElement("avProblems");
		if (hasElevationDependency) {
			Element avProblemAbove = doc.createElement("AvProblem");
			Element validElevationAbove = doc.createElement("validElevation");
			validElevationAbove.setAttribute("xlink:href",
					AlbinaUtil.createValidElevationAttribute(elevation, true, treeline));
			avProblemAbove.appendChild(validElevationAbove);
			if (above != null && above.getAvalancheProblem() != null) {
				Element typeAbove = doc.createElement("type");
				typeAbove.appendChild(doc.createTextNode(above.getAvalancheProblem().toCaamlString()));
				avProblemAbove.appendChild(typeAbove);
			}
			for (Aspect aspect : above.getAspects()) {
				Element validAspect = doc.createElement("validAspect");
				validAspect.setAttribute("xlink:href", aspect.toCaamlString());
				avProblemAbove.appendChild(validAspect);
			}

			avProblems.appendChild(avProblemAbove);

			Element avProblemBelow = doc.createElement("AvProblem");
			Element validElevationBelow = doc.createElement("validElevation");
			validElevationBelow.setAttribute("xlink:href",
					AlbinaUtil.createValidElevationAttribute(elevation, false, treeline));
			avProblemBelow.appendChild(validElevationBelow);
			if (below != null && below.getAvalancheProblem() != null) {
				Element typeBelow = doc.createElement("type");
				typeBelow.appendChild(doc.createTextNode(below.getAvalancheProblem().toCaamlString()));
				avProblemBelow.appendChild(typeBelow);
			}
			for (Aspect aspect : below.getAspects()) {
				Element validAspect = doc.createElement("validAspect");
				validAspect.setAttribute("xlink:href", aspect.toCaamlString());
				avProblemBelow.appendChild(validAspect);
			}
			avProblems.appendChild(avProblemBelow);
		} else {
			Element avProblem = doc.createElement("AvProblem");
			if (above != null && above.getAvalancheProblem() != null) {
				Element type = doc.createElement("type");
				type.appendChild(doc.createTextNode(above.getAvalancheProblem().toCaamlString()));
				avProblem.appendChild(type);
			}
			for (Aspect aspect : above.getAspects()) {
				Element validAspect = doc.createElement("validAspect");
				validAspect.setAttribute("xlink:href", aspect.toCaamlString());
				avProblem.appendChild(validAspect);
			}
			avProblems.appendChild(avProblem);
		}
		bulletinMeasurements.appendChild(avProblems);

		for (TextPart part : TextPart.values()) {
			if (textPartsMap.get(part) != null && textPartsMap.get(part).getTexts() != null
					&& (!textPartsMap.get(part).getTexts().isEmpty())
					&& (textPartsMap.get(part).getText(languageCode) != null)) {
				Element textPart = doc.createElement(part.toCaamlString());
				textPart.appendChild(doc.createTextNode(textPartsMap.get(part).getText(languageCode)));
				bulletinMeasurements.appendChild(textPart);
			}
		}

		bulletinResultsOf.appendChild(bulletinMeasurements);
		rootElement.appendChild(bulletinResultsOf);

		return rootElement;
	}

	public List<Element> toCAAML(Document doc, LanguageCode languageCode, DateTime startDate, int version) {
		if (!publishedRegions.isEmpty()) {
			List<Element> result = new ArrayList<Element>();
			result.add(createCAAMLBulletin(doc, languageCode, startDate, version, false));

			if (hasDaytimeDependency)
				result.add(createCAAMLBulletin(doc, languageCode, startDate, version, true));

			return result;
		} else
			return null;
	}

	public void copy(AvalancheBulletin bulletin) {
		setUser(bulletin.getUser());
		setCreator(bulletin.getCreator());
		setCreatorRegion(bulletin.getCreatorRegion());
		setPublicationDate(bulletin.getPublicationDate());
		setValidFrom(bulletin.getValidFrom());
		setValidUntil(bulletin.getValidUntil());
		setSuggestedRegions(bulletin.getSuggestedRegions());
		setPublishedRegions(bulletin.getPublishedRegions());
		setSavedRegions(bulletin.getSavedRegions());
		setElevation(bulletin.getElevation());
		setHasDaytimeDependency(bulletin.isHasDaytimeDependency());
		setHasElevationDependency(bulletin.isHasElevationDependency());

		if (bulletin.getForenoonAbove() != null) {
			if (forenoonAbove == null)
				forenoonAbove = bulletin.getForenoonAbove();
			else {
				forenoonAbove.setAspects(bulletin.getForenoonAbove().getAspects());
				forenoonAbove.setAvalancheProblem(bulletin.getForenoonAbove().getAvalancheProblem());
				forenoonAbove.setDangerRating(bulletin.getForenoonAbove().getDangerRating());
			}
		}

		if (bulletin.getForenoonBelow() != null) {
			if (forenoonBelow == null)
				forenoonBelow = bulletin.getForenoonBelow();
			else {
				forenoonBelow.setAspects(bulletin.getForenoonBelow().getAspects());
				forenoonBelow.setAvalancheProblem(bulletin.getForenoonBelow().getAvalancheProblem());
				forenoonBelow.setDangerRating(bulletin.getForenoonBelow().getDangerRating());
			}
		}

		if (bulletin.getAfternoonAbove() != null) {
			if (afternoonAbove == null)
				afternoonAbove = bulletin.getAfternoonAbove();
			else {
				afternoonAbove.setAspects(bulletin.getAfternoonAbove().getAspects());
				afternoonAbove.setAvalancheProblem(bulletin.getAfternoonAbove().getAvalancheProblem());
				afternoonAbove.setDangerRating(bulletin.getAfternoonAbove().getDangerRating());
			}
		}

		if (bulletin.getAfternoonBelow() != null) {
			if (afternoonBelow == null)
				afternoonBelow = bulletin.getAfternoonBelow();
			else {
				afternoonBelow.setAspects(bulletin.getAfternoonBelow().getAspects());
				afternoonBelow.setAvalancheProblem(bulletin.getAfternoonBelow().getAvalancheProblem());
				afternoonBelow.setDangerRating(bulletin.getAfternoonBelow().getDangerRating());
			}
		}

		textPartsMap = bulletin.textPartsMap;

		setAvActivityHighlightsTextcat(bulletin.getAvActivityHighlightsTextcat());
		setAvActivityCommentTextcat(bulletin.getAvActivityCommentTextcat());
	}
}
