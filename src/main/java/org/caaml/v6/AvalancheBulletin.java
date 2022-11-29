package org.caaml.v6;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;
import java.util.List;

/**
 * Avalanche Bulletin valid for a given set of regions.
 */
public class AvalancheBulletin {
    private Texts avalancheActivity;
    private List<AvalancheProblem> avalancheProblems;
    private String bulletinID;
    private CustomData[] customData;
    private List<DangerRating> dangerRatings;
    private String highlights;
    private String lang;
    private MetaData metaData;
	@JsonSerialize(using = ToStringSerializer.class)
    private Instant publicationTime;
    private List<Region> regions;
    private Texts snowpackStructure;
    private AvalancheBulletinSource source;
    private AvalancheSituationTendency tendency;
    private Texts travelAdvisory;
    private ValidTime validTime;
    private Texts wxSynopsis;

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

    public CustomData[] getCustomData() { return customData; }
    public void setCustomData(CustomData[] value) { this.customData = value; }

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
    public AvalancheSituationTendency getTendency() { return tendency; }
    public void setTendency(AvalancheSituationTendency value) { this.tendency = value; }

    /**
     * Texts element with highlight and comment for travel advisory.
     */
    public Texts getTravelAdvisory() { return travelAdvisory; }
    public void setTravelAdvisory(Texts value) { this.travelAdvisory = value; }

    /**
     * Date and Time from and until this bulletin is valid. ISO 8601 Timestamp in UTC or with
     * time zone information.
     */
    public ValidTime getValidTime() { return validTime; }
    public void setValidTime(ValidTime value) { this.validTime = value; }

    /**
     * Texts element with highlight and comment for weather forcast information.
     */
    public Texts getWxSynopsis() { return wxSynopsis; }
    public void setWxSynopsis(Texts value) { this.wxSynopsis = value; }
}
