// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Avalanche Bulletin valid for a given set of regions.
 */
@JsonPropertyOrder({"publicationTime", "validTime", "nextUpdate", "unscheduled", "source", "region", "dangerRating", "avalancheProblem", "highlights", "weatherForecast", "weatherReview", "avalancheActivity", "snowpackStructure", "travelAdvisory", "tendency", "metaData", "customData"})
@Serdeable
public class AvalancheBulletin {
    private Texts avalancheActivity;
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "avalancheProblem")
    private List<AvalancheProblem> avalancheProblems;
	@JacksonXmlProperty(isAttribute = true)
    private String bulletinID;
    private AvalancheBulletinCustomData customData;
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "dangerRating")
    private List<DangerRating> dangerRatings;
    private String highlights;
	@JacksonXmlProperty(isAttribute = true)
    private String lang;
    private MetaData metaData;
	@JacksonXmlProperty(localName = "nextUpdate")
    private Instant nextUpdate;
	@JacksonXmlProperty(localName = "publicationTime")
	private Instant publicationTime;
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "region")
    private List<Region> regions;
    private Texts snowpackStructure;
    private AvalancheBulletinSource source;
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "tendency")
    private List<Tendency> tendency;
    private Texts travelAdvisory;
    private Boolean unscheduled;
    private ValidTime validTime;
    private Texts weatherForecast;
    private Texts weatherReview;

    /**
     * Texts element with highlight and comment for the avalanche activity.
     */
    public Texts getAvalancheActivity() { return avalancheActivity; }
    public void setAvalancheActivity(Texts value) { this.avalancheActivity = value; }

    /**
     * Collection of Avalanche Problem elements for this bulletin.
     */
    public List<AvalancheProblem> getAvalancheProblems() { return avalancheProblems; }
    public void setAvalancheProblems(List<AvalancheProblem> value) { this.avalancheProblems = value; }

    /**
     * Unique ID for the bulletin.
     */
    public String getBulletinID() { return bulletinID; }
    public void setBulletinID(String value) { this.bulletinID = value; }

    public AvalancheBulletinCustomData getCustomData() { return customData; }
    public void setCustomData(AvalancheBulletinCustomData value) { this.customData = value; }

    /**
     * Collection of Danger Rating elements for this bulletin.
     */
    public List<DangerRating> getDangerRatings() { return dangerRatings; }
    public void setDangerRatings(List<DangerRating> value) { this.dangerRatings = value; }

    /**
     * Contains an optional short text to highlight an exceptionally dangerous situation.
     */
    public String getHighlights() { return highlights; }
    public void setHighlights(String value) { this.highlights = value; }

    /**
     * Two-letter language code (ISO 639-1).
     */
    public String getLang() { return lang; }
    public void setLang(String value) { this.lang = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    /**
     * Time and date when the next bulletin will be published by the AWS to the Public. ISO 8601
     * timestamp in UTC or with time zone information.
     */
    public Instant getNextUpdate() { return nextUpdate; }
    public void setNextUpdate(Instant value) { this.nextUpdate = value; }

    /**
     * Time and date when the bulletin was issued by the AWS to the Public. ISO 8601 timestamp
     * in UTC or with time zone information.
     */
    public Instant getPublicationTime() { return publicationTime; }
    public void setPublicationTime(Instant value) { this.publicationTime = value; }

    /**
     * Collection of region elements for which this bulletin is valid.
     */
    public List<Region> getRegions() { return regions; }
    public void setRegions(List<Region> value) { this.regions = value; }

    /**
     * Texts element with highlight and comment for details on the snowpack structure.
     */
    public Texts getSnowpackStructure() { return snowpackStructure; }
    public void setSnowpackStructure(Texts value) { this.snowpackStructure = value; }

    /**
     * Details about the issuer/AWS of the bulletin.
     */
    public AvalancheBulletinSource getSource() { return source; }
    public void setSource(AvalancheBulletinSource value) { this.source = value; }

    /**
     * Tendency element for a detailed description of the expected avalanche situation tendency
     * after the bulletin's period of validity.
     */
    public List<Tendency> getTendency() { return tendency; }
    public void setTendency(List<Tendency> value) { this.tendency = value; }

    /**
     * Texts element with highlight and comment for travel advisory.
     */
    public Texts getTravelAdvisory() { return travelAdvisory; }
    public void setTravelAdvisory(Texts value) { this.travelAdvisory = value; }

    /**
     * Flag if bulletin is unscheduled or not.
     */
    public Boolean getUnscheduled() { return unscheduled; }
    public void setUnscheduled(Boolean value) { this.unscheduled = value; }

    /**
     * Date and Time from and until this bulletin is valid. ISO 8601 Timestamp in UTC or with
     * time zone information.
     */
    public ValidTime getValidTime() { return validTime; }
    public void setValidTime(ValidTime value) { this.validTime = value; }

    /**
     * Texts element with highlight and comment for weather forecast information.
     */
    public Texts getWeatherForecast() { return weatherForecast; }
    public void setWeatherForecast(Texts value) { this.weatherForecast = value; }

    /**
     * Texts element with highlight and comment for weather review information.
     */
    public Texts getWeatherReview() { return weatherReview; }
    public void setWeatherReview(Texts value) { this.weatherReview = value; }

	/** Whether the danger ratings and the avalanche problems are split into forenoon and afternoon. */
	public boolean hasDaytimeDependency() {
		return dangerRatings.stream().anyMatch(dangerRating -> dangerRating.getValidTimePeriod() != ValidTimePeriod.all_day);
	}

	public List<DangerRating> dangerRatings(ValidTimePeriod validTimePeriod) {
		return dangerRatings.stream().filter(dangerRating -> dangerRating.getValidTimePeriod() == validTimePeriod).toList();
	}

	public List<AvalancheProblem> avalancheProblems(ValidTimePeriod validTimePeriod) {
		return avalancheProblems.stream().filter(problem -> problem.getValidTimePeriod() == validTimePeriod).toList();
	}

	/** The highest danger rating of all daytimes and elevations, or null if there is none. */
	public DangerRatingValue highestDangerRating() {
		return dangerRatings.stream()
			.map(DangerRating::getMainValue)
			.filter(Objects::nonNull)
			.max(DangerRatingValue.BY_SEVERITY)
			.orElse(null);
	}

	/** The danger rating above resp. below the elevation boundary of the given daytime, or null if there is none. */
	public DangerRatingValue dangerRating(ValidTimePeriod validTimePeriod, boolean above) {
		return dangerRatings(validTimePeriod).stream()
			.filter(dangerRating -> dangerRating.getElevation() == null
				|| (above ? dangerRating.getElevation().getLowerBound() : dangerRating.getElevation().getUpperBound()) != null)
			.map(DangerRating::getMainValue)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	/** The elevation boundary separating the danger ratings of the given daytime, or null if there is none. */
	public String dangerRatingElevation(ValidTimePeriod validTimePeriod) {
		return dangerRatings(validTimePeriod).stream()
			.map(DangerRating::getElevation)
			.filter(Objects::nonNull)
			.map(ElevationBoundaryOrBand::getLowerBound)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	/** The danger patterns of the LWD Tyrol custom data, or an empty list if there are none. */
	public List<String> dangerPatterns() {
		return customData == null || customData.LWD_Tyrol() == null || customData.LWD_Tyrol().dangerPatterns() == null
			? List.of()
			: customData.LWD_Tyrol().dangerPatterns();
	}
}
