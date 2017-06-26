package eu.albina.model;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.controller.UserController;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.TextPart;
import eu.albina.util.AlbinaUtil;
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
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	private User user;

	/** Validity of the avalanche bulletin */
	@Column(name = "VALID_FROM")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime validFrom;
	@Column(name = "VALID_UNTIL")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime validUntil;

	@Column(name = "AGGREGATED_REGION_ID")
	private String aggregatedRegionId;

	/** The regions the avalanche bulletin is for. */
	@ElementCollection
	@CollectionTable(name = "AVALANCHE_BULLETIN_REGIONS", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID")
	private Set<String> regions;

	@Column(name = "ELEVATION")
	private int elevation;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS")
	private BulletinStatus status;

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

		if (json.has("aggregatedRegionId"))
			this.aggregatedRegionId = json.getString("aggregatedRegionId");

		if (json.has("regions")) {
			JSONArray regions = json.getJSONArray("regions");
			for (Object entry : regions) {
				this.regions.add((String) entry);
			}
		}

		if (json.has("elevation"))
			this.elevation = json.getInt("elevation");

		if (json.has("above"))
			this.above = new AvalancheBulletinElevationDescription(json.getJSONObject("above"));

		if (json.has("below"))
			this.below = new AvalancheBulletinElevationDescription(json.getJSONObject("below"));

		if (json.has("status"))
			this.status = BulletinStatus.fromString(json.getString("status"));
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public String getAggregatedRegionId() {
		return aggregatedRegionId;
	}

	public void setAggregatedRegionId(String aggregatedRegionId) {
		this.aggregatedRegionId = aggregatedRegionId;
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

	public BulletinStatus getStatus() {
		return status;
	}

	public void setStatus(BulletinStatus status) {
		this.status = status;
	}

	public int getElevation() {
		return elevation;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (id != null && id != "")
			json.put("id", id);

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

		json.put("aggregatedRegionId", aggregatedRegionId);

		json.put("regions", regions);
		JSONArray regions = new JSONArray();
		for (Object region : regions) {
			regions.put(region);
		}

		json.put("elevation", elevation);
		if (above != null)
			json.put("above", above.toJSON());
		if (below != null)
			json.put("below", below.toJSON());
		json.put("status", status);

		return json;
	}

	public Element toCAAML(Document doc, LanguageCode languageCode) {
		Element rootElement = doc.createElement("Bulletin");
		rootElement.setAttribute("gml:id", getId());
		if (languageCode == null)
			languageCode = LanguageCode.en;
		rootElement.setAttribute("xml:lang", languageCode.toString());

		Element metaDataProperty = doc.createElement("metaDataProperty");
		Element metaData = doc.createElement("MetaData");
		Element dateTimeReport = doc.createElement("dateTimeReport");
		// TODO use datetimeformatter from global variables
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		dateTimeReport.appendChild(doc.createTextNode(dt.format(new Date())));
		metaData.appendChild(dateTimeReport);
		Element srcRef = doc.createElement("srcRef");
		srcRef.appendChild(user.toCAAML(doc));
		metaData.appendChild(srcRef);
		metaDataProperty.appendChild(metaData);
		rootElement.appendChild(metaDataProperty);

		for (String region : regions) {
			Element locRef = doc.createElement("locRef");
			locRef.setAttribute("xlink:href", region);
			rootElement.appendChild(locRef);
		}

		Element validTime = doc.createElement("validTime");
		Element timePeriod = doc.createElement("TimePeriod");
		Element beginPosition = doc.createElement("beginPosition");
		beginPosition.appendChild(doc.createTextNode(validFrom.toString(GlobalVariables.formatterDateTime)));
		timePeriod.appendChild(beginPosition);
		Element endPosition = doc.createElement("endPosition");
		endPosition.appendChild(doc.createTextNode(validUntil.toString(GlobalVariables.formatterDateTime)));
		timePeriod.appendChild(endPosition);
		validTime.appendChild(timePeriod);
		rootElement.appendChild(validTime);

		Element bulletinResultsOf = doc.createElement("bulletinResultsOf");
		Element bulletinMeasurements = doc.createElement("BulletinMeasurements");

		Element dangerRatings = doc.createElement("dangerRatings");
		if (elevation > 0) {
			Element dangerRatingAbove = doc.createElement("DangerRating");
			Element validElevationAbove = doc.createElement("validElevation");
			validElevationAbove.setAttribute("xlink:href", AlbinaUtil.createValidElevationAttribute(elevation, true));
			dangerRatingAbove.appendChild(validElevationAbove);
			Element mainValueAbove = doc.createElement("mainValue");
			mainValueAbove.appendChild(doc.createTextNode(String.valueOf(above.getDangerRating())));
			dangerRatingAbove.appendChild(mainValueAbove);
			dangerRatings.appendChild(dangerRatingAbove);
			Element dangerRatingBelow = doc.createElement("DangerRating");
			Element validElevationBelow = doc.createElement("validElevation");
			validElevationBelow.setAttribute("xlink:href", AlbinaUtil.createValidElevationAttribute(elevation, false));
			dangerRatingBelow.appendChild(validElevationBelow);
			Element mainValueBelow = doc.createElement("mainValue");
			mainValueBelow.appendChild(doc.createTextNode(String.valueOf(below.getDangerRating())));
			dangerRatingBelow.appendChild(mainValueBelow);
			dangerRatings.appendChild(dangerRatingBelow);
		} else {
			// NOTE if no elevation is set, the elevation description is
			// above
			Element dangerRating = doc.createElement("DangerRating");
			Element mainValue = doc.createElement("mainValue");
			mainValue.appendChild(doc.createTextNode(String.valueOf(above.getDangerRating())));
			dangerRating.appendChild(mainValue);
			dangerRatings.appendChild(dangerRating);
		}
		bulletinMeasurements.appendChild(dangerRatings);

		Element avProblems = doc.createElement("avProblems");
		if (elevation > 0) {
			Element avProblemAbove = doc.createElement("AvProblem");
			Element validElevationAbove = doc.createElement("validElevation");
			validElevationAbove.setAttribute("xlink:href", AlbinaUtil.createValidElevationAttribute(elevation, true));
			avProblemAbove.appendChild(validElevationAbove);
			Element typeAbove = doc.createElement("type");
			typeAbove.appendChild(doc.createTextNode(above.getAvalancheProblem().toCaamlString()));
			avProblemAbove.appendChild(typeAbove);
			for (Aspect aspect : above.getAspects()) {
				Element validAspect = doc.createElement("validAspect");
				validAspect.setAttribute("xlink:href", aspect.toCaamlString());
				avProblemAbove.appendChild(validAspect);
			}
			avProblems.appendChild(avProblemAbove);

			Element avProblemBelow = doc.createElement("AvProblem");
			Element validElevationBelow = doc.createElement("validElevation");
			validElevationBelow.setAttribute("xlink:href", AlbinaUtil.createValidElevationAttribute(elevation, false));
			avProblemBelow.appendChild(validElevationBelow);
			Element typeBelow = doc.createElement("type");
			typeBelow.appendChild(doc.createTextNode(below.getAvalancheProblem().toCaamlString()));
			avProblemBelow.appendChild(typeBelow);
			for (Aspect aspect : below.getAspects()) {
				Element validAspect = doc.createElement("validAspect");
				validAspect.setAttribute("xlink:href", aspect.toCaamlString());
				avProblemBelow.appendChild(validAspect);
			}
			avProblems.appendChild(avProblemBelow);
		} else {
			Element avProblem = doc.createElement("AvProblem");
			Element type = doc.createElement("type");
			type.appendChild(doc.createTextNode(above.getAvalancheProblem().toCaamlString()));
			avProblem.appendChild(type);
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
					&& (!textPartsMap.get(part).getTexts().isEmpty())) {
				Element textPart = doc.createElement(part.toCaamlString());
				textPart.appendChild(doc.createTextNode(textPartsMap.get(part).getText(languageCode)));
				bulletinMeasurements.appendChild(textPart);
			}
		}

		bulletinResultsOf.appendChild(bulletinMeasurements);
		rootElement.appendChild(bulletinResultsOf);

		return rootElement;
	}
}
