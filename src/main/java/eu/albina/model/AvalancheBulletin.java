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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import eu.albina.util.LinkUtil;
import com.google.common.base.Strings;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.common.io.Resources;

import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.Complexity;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;
import eu.albina.model.enumerations.TextPart;
import eu.albina.util.XmlUtil;

/**
 * This class holds all information about one avalanche bulletin.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "avalanche_bulletins")
public class AvalancheBulletin extends AbstractPersistentObject
		implements AvalancheInformationObject, Comparable<AvalancheBulletin> {

	/** Information about the author of the avalanche bulletin */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	private User user;

	@Column(name = "OWNER_REGION")
	private String ownerRegion;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "avalanche_bulletin_additional_user", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "ADDITIONAL_USER_NAME")
	private Set<String> additionalAuthors;

	@Column(name = "PUBLICATION_DATE")
	private ZonedDateTime publicationDate;

	/** Validity of the avalanche bulletin */
	@Column(name = "VALID_FROM")
	private ZonedDateTime validFrom;
	@Column(name = "VALID_UNTIL")
	private ZonedDateTime validUntil;

	/** The recommended regions the avalanche bulletin is for. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "avalanche_bulletin_suggested_regions", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID")
	private Set<String> suggestedRegions;

	/** The published regions the avalanche bulletin is for. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "avalanche_bulletin_published_regions", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID")
	private Set<String> publishedRegions;

	/** The saved regions the avalanche bulletin is for. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "avalanche_bulletin_saved_regions", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID")
	private Set<String> savedRegions;

	@Column(name = "HAS_DAYTIME_DEPENDENCY")
	private boolean hasDaytimeDependency;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "FORENOON_ID")
	private AvalancheBulletinDaytimeDescription forenoon;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AFTERNOON_ID")
	private AvalancheBulletinDaytimeDescription afternoon;

	@Lob
	@Column(name = "HIGHLIGHTS_TEXTCAT")
	private String highlightsTextcat;

	@Lob
	@Column(name = "AV_ACTIVITY_HIGHLIGHTS_TEXTCAT")
	private String avActivityHighlightsTextcat;

	@Lob
	@Column(name = "AV_ACTIVITY_COMMENT_TEXTCAT")
	private String avActivityCommentTextcat;

	@Lob
	@Column(name = "SNOWPACK_STRUCTURE_HIGHLIGHTS_TEXTCAT")
	private String snowpackStructureHighlightsTextcat;

	@Lob
	@Column(name = "SNOWPACK_STRUCTURE_COMMENT_TEXTCAT")
	private String snowpackStructureCommentTextcat;

	@Lob
	@Column(name = "TENDENCY_COMMENT_TEXTCAT")
	private String tendencyCommentTextcat;

	@Lob
	@Column(name = "AV_ACTIVITY_HIGHLIGHTS_NOTES")
	private String avActivityHighlightsNotes;

	@Lob
	@Column(name = "AV_ACTIVITY_COMMENT_NOTES")
	private String avActivityCommentNotes;

	@Lob
	@Column(name = "SNOWPACK_STRUCTURE_HIGHLIGHTS_NOTES")
	private String snowpackStructureHighlightsNotes;

	@Lob
	@Column(name = "SNOWPACK_STRUCTURE_COMMENT_NOTES")
	private String snowpackStructureCommentNotes;

	@Lob
	@Column(name = "TENDENCY_COMMENT_NOTES")
	private String tendencyCommentNotes;

	@Enumerated(EnumType.STRING)
	@Column(name = "TENDENCY")
	private Tendency tendency;

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
		additionalAuthors = new LinkedHashSet<>();
		textPartsMap = new LinkedHashMap<>();
		publishedRegions = new LinkedHashSet<>();
		savedRegions = new LinkedHashSet<>();
		suggestedRegions = new LinkedHashSet<>();
	}

	/**
	 * Custom constructor that creates an avalanche bulletin object from JSON input.
	 *
	 * @param json
	 *            JSONObject holding information about an avalanche bulletin.
	 */
	public AvalancheBulletin(JSONObject json, Function<String, User> userFunction) {
		this();

		if (json.has("id"))
			this.id = json.getString("id");

		if (json.has("author")) {
			JSONObject author = json.getJSONObject("author");
			if (author.has("email")) {
				try {
					this.user = userFunction.apply(author.getString("email"));
				} catch (Exception e) {
					LoggerFactory.getLogger(getClass()).warn("Failed to get user", e);
				}
			}
		}

		if (json.has("additionalAuthors")) {
			JSONArray additionalAuthors = json.getJSONArray("additionalAuthors");
			for (Object entry : additionalAuthors) {
				this.additionalAuthors.add((String) entry);
			}
		}

		if (json.has("ownerRegion"))
			this.ownerRegion = json.getString("ownerRegion");
		if (json.has("highlightsTextcat"))
			this.highlightsTextcat = json.getString("highlightsTextcat");
		if (json.has("avActivityHighlightsTextcat"))
			this.avActivityHighlightsTextcat = json.getString("avActivityHighlightsTextcat");
		if (json.has("avActivityCommentTextcat"))
			this.avActivityCommentTextcat = json.getString("avActivityCommentTextcat");
		if (json.has("snowpackStructureHighlightsTextcat"))
			this.snowpackStructureHighlightsTextcat = json.getString("snowpackStructureHighlightsTextcat");
		if (json.has("snowpackStructureCommentTextcat"))
			this.snowpackStructureCommentTextcat = json.getString("snowpackStructureCommentTextcat");
		if (json.has("tendencyCommentTextcat"))
			this.tendencyCommentTextcat = json.getString("tendencyCommentTextcat");
		if (json.has("avActivityHighlightsNotes"))
			this.avActivityHighlightsNotes = json.getString("avActivityHighlightsNotes");
		if (json.has("avActivityCommentNotes"))
			this.avActivityCommentNotes = json.getString("avActivityCommentNotes");
		if (json.has("snowpackStructureHighlightsNotes"))
			this.snowpackStructureHighlightsNotes = json.getString("snowpackStructureHighlightsNotes");
		if (json.has("snowpackStructureCommentNotes"))
			this.snowpackStructureCommentNotes = json.getString("snowpackStructureCommentNotes");
		if (json.has("tendencyCommentNotes"))
			this.tendencyCommentNotes = json.getString("tendencyCommentNotes");

		if (json.has("tendency"))
			this.tendency = Tendency.valueOf(json.getString("tendency").toLowerCase());

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
			this.publicationDate = ZonedDateTime.parse(json.getString("publicationDate"));

		if (json.has("validity")) {
			JSONObject validity = json.getJSONObject("validity");
			this.validFrom = ZonedDateTime.parse(validity.getString("from"));
			this.validUntil = ZonedDateTime.parse(validity.getString("until"));
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
		} else if (json.has("regions")) {
			JSONArray regions = json.getJSONArray("regions");
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

		if (json.has("forenoon"))
			this.forenoon = new AvalancheBulletinDaytimeDescription(json.getJSONObject("forenoon"));

		if (json.has("afternoon"))
			this.afternoon = new AvalancheBulletinDaytimeDescription(json.getJSONObject("afternoon"));
	}

	public Set<String> regions(boolean preview) {
		return preview ? getPublishedAndSavedRegions() : getPublishedRegions();
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Set<String> getAdditionalAuthors() {
		return additionalAuthors;
	}

	public void setAdditionalAuthors(Set<String> additionalAuthors) {
		this.additionalAuthors = additionalAuthors;
	}

	public void addAdditionalAuthor(String additionalAuthor) {
		if (!this.additionalAuthors.contains(additionalAuthor))
			this.additionalAuthors.add(additionalAuthor);
	}

	public String getOwnerRegion() {
		return ownerRegion;
	}

	public void setOwnerRegion(String ownerRegion) {
		this.ownerRegion = ownerRegion;
	}

	public Map<TextPart, Texts> getTextPartsMap() {
		return textPartsMap;
	}

	public String getHighlightsTextcat() {
		return highlightsTextcat;
	}

	public void setHighlightsTextcat(String highlightsTextcat) {
		this.highlightsTextcat = highlightsTextcat;
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

	public String getTendencyCommentTextcat() {
		return tendencyCommentTextcat;
	}

	public void setTendencyCommentTextcat(String tendencyCommentTextcat) {
		this.tendencyCommentTextcat = tendencyCommentTextcat;
	}

	public String getAvActivityHighlightsNotes() {
		return avActivityHighlightsNotes;
	}

	public void setAvActivityHighlightsNotes(String avActivityHighlightsNotes) {
		this.avActivityHighlightsNotes = avActivityHighlightsNotes;
	}

	public String getAvActivityCommentNotes() {
		return avActivityCommentNotes;
	}

	public void setAvActivityCommentNotes(String avActivityCommentNotes) {
		this.avActivityCommentNotes = avActivityCommentNotes;
	}

	public String getSnowpackStructureHighlightsNotes() {
		return snowpackStructureHighlightsNotes;
	}

	public void setSnowpackStructureHighlightsNotes(String snowpackStructureHighlightsNotes) {
		this.snowpackStructureHighlightsNotes = snowpackStructureHighlightsNotes;
	}

	public String getSnowpackStructureCommentNotes() {
		return snowpackStructureCommentNotes;
	}

	public void setSnowpackStructureCommentNotes(String snowpackStructureCommentNotes) {
		this.snowpackStructureCommentNotes = snowpackStructureCommentNotes;
	}

	public String getTendencyCommentNotes() {
		return tendencyCommentNotes;
	}

	public void setTendencyCommentNotes(String tendencyCommentNotes) {
		this.tendencyCommentNotes = tendencyCommentNotes;
	}

	public Texts getHighlights() {
		return textPartsMap.get(TextPart.highlights);
	}

	public String getHighlightsIn(LanguageCode lang) {
		Texts texts = textPartsMap.get(TextPart.highlights);
		if (texts != null)
			return texts.getText(lang);
		else
			return null;
	}

	public void setHighlights(Texts highlights) {
		textPartsMap.put(TextPart.highlights, highlights);
	}

	public Texts getAvActivityHighlights() {
		return textPartsMap.get(TextPart.avActivityHighlights);
	}

	public String getAvActivityHighlightsIn(LanguageCode lang) {
		Texts texts = textPartsMap.get(TextPart.avActivityHighlights);
		if (texts != null)
			return texts.getText(lang);
		else
			return null;
	}

	public void setAvActivityHighlights(Texts avActivityHighlights) {
		textPartsMap.put(TextPart.avActivityHighlights, avActivityHighlights);
	}

	public Texts getAvActivityComment() {
		return textPartsMap.get(TextPart.avActivityComment);
	}

	public String getAvActivityCommentIn(LanguageCode lang) {
		Texts texts = textPartsMap.get(TextPart.avActivityComment);
		if (texts != null)
			return texts.getText(lang);
		else
			return null;
	}

	public void setAvActivityComment(Texts avActivityComment) {
		textPartsMap.put(TextPart.avActivityComment, avActivityComment);
	}

	public Texts getSynopsisHighlights() {
		return textPartsMap.get(TextPart.synopsisHighlights);
	}

	public String getSynopsisHighlightsIn(LanguageCode lang) {
		Texts texts = textPartsMap.get(TextPart.synopsisHighlights);
		if (texts != null)
			return texts.getText(lang);
		else
			return null;
	}

	public void setSynopsisHighlights(Texts synopsisHighlights) {
		textPartsMap.put(TextPart.synopsisHighlights, synopsisHighlights);
	}

	public Texts getSynopsisComment() {
		return textPartsMap.get(TextPart.synopsisComment);
	}

	public String getSynopsisCommentIn(LanguageCode lang) {
		Texts texts = textPartsMap.get(TextPart.synopsisComment);
		if (texts != null)
			return texts.getText(lang);
		else
			return null;
	}

	public void setSynopsisComment(Texts synopsisComment) {
		textPartsMap.put(TextPart.synopsisComment, synopsisComment);
	}

	public Texts getSnowpackStructureHighlights() {
		return textPartsMap.get(TextPart.snowpackStructureHighlights);
	}

	public String getSnowpackStructureHighlightsIn(LanguageCode lang) {
		Texts texts = textPartsMap.get(TextPart.snowpackStructureHighlights);
		if (texts != null)
			return texts.getText(lang);
		else
			return null;
	}

	public void setSnowpackStructureHighlights(Texts snowpackStructureHighlights) {
		textPartsMap.put(TextPart.snowpackStructureHighlights, snowpackStructureHighlights);
	}

	public Texts getSnowpackStructureComment() {
		return textPartsMap.get(TextPart.snowpackStructureComment);
	}

	public String getSnowpackStructureCommentIn(LanguageCode lang) {
		Texts texts = textPartsMap.get(TextPart.snowpackStructureComment);
		if (texts != null)
			return texts.getText(lang);
		else
			return null;
	}

	public void setSnowpackStructureComment(Texts snowpackStructureComment) {
		textPartsMap.put(TextPart.snowpackStructureComment, snowpackStructureComment);
	}

	public Texts getTravelAdvisoryHighlights() {
		return textPartsMap.get(TextPart.travelAdvisoryHighlights);
	}

	public String getTravelAdvisoryHighlightsIn(LanguageCode lang) {
		Texts texts = textPartsMap.get(TextPart.travelAdvisoryHighlights);
		return texts.getText(lang);
	}

	public void setTravelAdvisoryHighlights(Texts travelAdvisoryHighlights) {
		textPartsMap.put(TextPart.travelAdvisoryHighlights, travelAdvisoryHighlights);
	}

	public Texts getTravelAdvisoryComment() {
		return textPartsMap.get(TextPart.travelAdvisoryComment);
	}

	public String getTravelAdvisoryCommentIn(LanguageCode lang) {
		Texts texts = textPartsMap.get(TextPart.travelAdvisoryComment);
		return texts.getText(lang);
	}

	public void setTravelAdvisoryComment(Texts travelAdvisoryComment) {
		textPartsMap.put(TextPart.travelAdvisoryComment, travelAdvisoryComment);
	}

	public Texts getTendencyComment() {
		return textPartsMap.get(TextPart.tendencyComment);
	}

	public String getTendencyCommentIn(LanguageCode lang) {
		Texts texts = textPartsMap.get(TextPart.tendencyComment);
		if (texts != null)
			return texts.getText(lang);
		else
			return null;
	}

	public void setTendencyComment(Texts tendencyComment) {
		textPartsMap.put(TextPart.tendencyComment, tendencyComment);
	}

	public Tendency getTendency() {
		return tendency;
	}

	public void setTendency(Tendency tendency) {
		this.tendency = tendency;
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

	public ZonedDateTime getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(ZonedDateTime publicationDate) {
		this.publicationDate = publicationDate;
	}

	public ZonedDateTime getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(ZonedDateTime validFrom) {
		this.validFrom = validFrom;
	}

	public ZonedDateTime getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(ZonedDateTime validUntil) {
		this.validUntil = validUntil;
	}

	public Set<String> getSuggestedRegions() {
		return suggestedRegions;
	}

	public void setSuggestedRegions(Set<String> regions) {
		this.suggestedRegions = regions;
	}

	public void addSuggestedRegion(String region) {
		this.suggestedRegions.add(region);
	}

	public Set<String> getSavedRegions() {
		return savedRegions;
	}

	public void setSavedRegions(Set<String> regions) {
		this.savedRegions = regions;
	}

	public void addSavedRegion(String region) {
		this.savedRegions.add(region);
	}

	public Set<String> getPublishedRegions() {
		return publishedRegions;
	}

	public void setPublishedRegions(Set<String> regions) {
		this.publishedRegions = regions;
	}

	public void addPublishedRegion(String region) {
		this.publishedRegions.add(region);
	}

	public AvalancheBulletinDaytimeDescription getForenoon() {
		return forenoon;
	}

	public void setForenoon(AvalancheBulletinDaytimeDescription forenoon) {
		this.forenoon = forenoon;
	}

	public AvalancheBulletinDaytimeDescription getAfternoon() {
		return afternoon;
	}

	public void setAfternoon(AvalancheBulletinDaytimeDescription afternoon) {
		this.afternoon = afternoon;
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

	public Set<String> getPublishedAndSavedRegions() {
		Set<String> result = new LinkedHashSet<>();
		result.addAll(savedRegions);
		result.addAll(publishedRegions);
		return result;
	}

	public boolean isHasDaytimeDependency() {
		return hasDaytimeDependency;
	}

	public void setHasDaytimeDependency(boolean hasDaytimeDependency) {
		this.hasDaytimeDependency = hasDaytimeDependency;
	}

	public boolean affectsRegion(Region region) {
		return getSuggestedRegions().stream().anyMatch(suggestedRegion -> region.affects(suggestedRegion))
			|| getSavedRegions().stream().anyMatch(savedRegion -> region.affects(savedRegion))
			|| getPublishedRegions().stream().anyMatch(publishedRegion -> region.affects(publishedRegion));
	}

	public boolean affectsRegionWithoutSuggestions(Region region) {
		return getSavedRegions().stream().anyMatch(savedRegion -> region.affects(savedRegion))
			|| getPublishedRegions().stream().anyMatch(publishedRegion -> region.affects(publishedRegion));
	}

	public boolean affectsRegionOnlyPublished(Region region) {
		return getPublishedRegions().stream().anyMatch(publishedRegion -> region.affects(publishedRegion));
	}

	public static DangerRating getHighestDangerRating(List<AvalancheBulletin> bulletins) {
		return bulletins.stream()
			.map(AvalancheBulletin::getHighestDangerRating)
			.max(Comparator.naturalOrder())
			.orElse(DangerRating.missing);
	}

	public DangerRating getHighestDangerRating() {
		DangerRating result = DangerRating.missing;
		if (forenoon != null && forenoon.getDangerRatingAbove() != null
				&& result.compareTo(forenoon.getDangerRatingAbove()) < 0)
			result = forenoon.getDangerRatingAbove();
		if (forenoon != null && forenoon.getDangerRatingBelow() != null
				&& result.compareTo(forenoon.getDangerRatingBelow()) < 0)
			result = forenoon.getDangerRatingBelow();
		if (hasDaytimeDependency) {
			if (afternoon != null && afternoon.getDangerRatingAbove() != null
					&& result.compareTo(afternoon.getDangerRatingAbove()) < 0)
				result = afternoon.getDangerRatingAbove();
			if (afternoon != null && afternoon.getDangerRatingBelow() != null
					&& result.compareTo(afternoon.getDangerRatingBelow()) < 0)
				result = afternoon.getDangerRatingBelow();
		}
		return result;
	}

	public int getHighestDangerRatingDouble() {
		int sum = 0;
		if (forenoon != null) {
			if (forenoon.getDangerRatingAbove() != null)
				sum += DangerRating.getInt(forenoon.getDangerRatingAbove());
			if (forenoon.getDangerRatingBelow() != null)
				sum += DangerRating.getInt(forenoon.getDangerRatingBelow());
			else
				sum += DangerRating.getInt(forenoon.getDangerRatingAbove());
		}
		if (afternoon != null) {
			if (afternoon.getDangerRatingAbove() != null)
				sum += DangerRating.getInt(afternoon.getDangerRatingAbove());
			if (afternoon.getDangerRatingBelow() != null)
				sum += DangerRating.getInt(afternoon.getDangerRatingBelow());
			else
				sum += DangerRating.getInt(afternoon.getDangerRatingAbove());
		} else if (forenoon != null) {
			if (forenoon.getDangerRatingAbove() != null)
				sum += DangerRating.getInt(forenoon.getDangerRatingAbove());
			if (forenoon.getDangerRatingBelow() != null)
				sum += DangerRating.getInt(forenoon.getDangerRatingBelow());
			else
				sum += DangerRating.getInt(forenoon.getDangerRatingAbove());
		}

		return sum;
	}

	public ZonedDateTime getValidityDate() {
		ZonedDateTime date = validFrom;
		if (validFrom.getHour() > 12)
			date = date.plusDays(1);
		return date;
	}

	public String getValidityDateString() {
		return getValidityDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (!Strings.isNullOrEmpty(id))
			json.put("id", id);

		if (user != null && !Strings.isNullOrEmpty(user.getName()))
			json.put("author", user.toSmallJSON());

		if (additionalAuthors != null && additionalAuthors.size() > 0) {
			JSONArray users = new JSONArray();
			for (String user : additionalAuthors) {
				users.put(user);
			}
			json.put("additionalAuthors", users);
		}

		if (user != null && user.getRoles() != null)
			json.put("ownerRegion", ownerRegion);

		if (!Strings.isNullOrEmpty(highlightsTextcat))
			json.put("highlightsTextcat", highlightsTextcat);
		if (!Strings.isNullOrEmpty(avActivityHighlightsTextcat))
			json.put("avActivityHighlightsTextcat", avActivityHighlightsTextcat);
		if (!Strings.isNullOrEmpty(avActivityCommentTextcat))
			json.put("avActivityCommentTextcat", avActivityCommentTextcat);
		if (!Strings.isNullOrEmpty(snowpackStructureHighlightsTextcat))
			json.put("snowpackStructureHighlightsTextcat", snowpackStructureHighlightsTextcat);
		if (!Strings.isNullOrEmpty(snowpackStructureCommentTextcat))
			json.put("snowpackStructureCommentTextcat", snowpackStructureCommentTextcat);
		if (!Strings.isNullOrEmpty(tendencyCommentTextcat))
			json.put("tendencyCommentTextcat", tendencyCommentTextcat);

		if (!Strings.isNullOrEmpty(avActivityHighlightsNotes))
			json.put("avActivityHighlightsNotes", avActivityHighlightsNotes);
		if (!Strings.isNullOrEmpty(avActivityCommentNotes))
			json.put("avActivityCommentNotes", avActivityCommentNotes);
		if (!Strings.isNullOrEmpty(snowpackStructureHighlightsNotes))
			json.put("snowpackStructureHighlightsNotes", snowpackStructureHighlightsNotes);
		if (!Strings.isNullOrEmpty(snowpackStructureCommentNotes))
			json.put("snowpackStructureCommentNotes", snowpackStructureCommentNotes);
		if (!Strings.isNullOrEmpty(tendencyCommentNotes))
			json.put("tendencyCommentNotes", tendencyCommentNotes);

		for (TextPart part : TextPart.values())
			if ((textPartsMap.get(part) != null))
				json.put(part.toString(), textPartsMap.get(part).toJSONArray());

		if (tendency != null)
			json.put("tendency", this.tendency.toString());

		if (dangerPattern1 != null)
			json.put("dangerPattern1", this.dangerPattern1.toString());
		if (dangerPattern2 != null)
			json.put("dangerPattern2", this.dangerPattern2.toString());

		if (publicationDate != null)
			json.put("publicationDate",	DateTimeFormatter.ISO_INSTANT.format(publicationDate));

		JSONObject validity = new JSONObject();
		validity.put("from", DateTimeFormatter.ISO_INSTANT.format(validFrom));
		validity.put("until", DateTimeFormatter.ISO_INSTANT.format(validUntil));
		json.put("validity", validity);

		json.put("suggestedRegions", suggestedRegions);
		json.put("savedRegions", savedRegions);
		json.put("publishedRegions", publishedRegions);

		json.put("hasDaytimeDependency", hasDaytimeDependency);

		if (forenoon != null)
			json.put("forenoon", forenoon.toJSON());

		if (hasDaytimeDependency && afternoon != null)
			json.put("afternoon", afternoon.toJSON());

		return json;
	}

	public JSONObject toSmallJSON() {
		JSONObject json = new JSONObject();

		if (!Strings.isNullOrEmpty(id))
			json.put("id", id);

		if (!Strings.isNullOrEmpty(highlightsTextcat))
			json.put("highlightsTextcat", highlightsTextcat);
		if (!Strings.isNullOrEmpty(avActivityHighlightsTextcat))
			json.put("avActivityHighlightsTextcat", avActivityHighlightsTextcat);
		if (!Strings.isNullOrEmpty(avActivityCommentTextcat))
			json.put("avActivityCommentTextcat", avActivityCommentTextcat);
		if (!Strings.isNullOrEmpty(snowpackStructureHighlightsTextcat))
			json.put("snowpackStructureHighlightsTextcat", snowpackStructureHighlightsTextcat);
		if (!Strings.isNullOrEmpty(snowpackStructureCommentTextcat))
			json.put("snowpackStructureCommentTextcat", snowpackStructureCommentTextcat);
		if (!Strings.isNullOrEmpty(tendencyCommentTextcat))
			json.put("tendencyCommentTextcat", tendencyCommentTextcat);

		if (!Strings.isNullOrEmpty(avActivityHighlightsNotes))
			json.put("avActivityHighlightsNotes", avActivityHighlightsNotes);
		if (!Strings.isNullOrEmpty(avActivityCommentNotes))
			json.put("avActivityCommentNotes", avActivityCommentNotes);
		if (!Strings.isNullOrEmpty(snowpackStructureHighlightsNotes))
			json.put("snowpackStructureHighlightsNotes", snowpackStructureHighlightsNotes);
		if (!Strings.isNullOrEmpty(snowpackStructureCommentNotes))
			json.put("snowpackStructureCommentNotes", snowpackStructureCommentNotes);
		if (!Strings.isNullOrEmpty(tendencyCommentNotes))
			json.put("tendencyCommentNotes", tendencyCommentNotes);

		for (TextPart part : TextPart.values())
			if ((textPartsMap.get(part) != null))
				json.put(part.toString(), textPartsMap.get(part).toJSONArray());

		if (tendency != null)
			json.put("tendency", this.tendency.toString());

		if (dangerPattern1 != null)
			json.put("dangerPattern1", this.dangerPattern1.toString());
		if (dangerPattern2 != null)
			json.put("dangerPattern2", this.dangerPattern2.toString());

		if (publicationDate != null)
			json.put("publicationDate",
				DateTimeFormatter.ISO_INSTANT.format(publicationDate));

		JSONObject validity = new JSONObject();
		validity.put("from", DateTimeFormatter.ISO_INSTANT.format(validFrom));
		validity.put("until", DateTimeFormatter.ISO_INSTANT.format(validUntil));
		json.put("validity", validity);

		json.put("regions", publishedRegions);

		json.put("hasDaytimeDependency", hasDaytimeDependency);

		if (forenoon != null)
			json.put("forenoon", forenoon.toSmallJSON());

		if (hasDaytimeDependency && afternoon != null)
			json.put("afternoon", afternoon.toSmallJSON());

		return json;
	}

	private Element createCAAMLv5Bulletin(Document doc, LanguageCode languageCode, Region region,  boolean isAfternoon) {
		AvalancheBulletinDaytimeDescription bulletinDaytimeDescription;

		if (isAfternoon)
			bulletinDaytimeDescription = this.afternoon;
		else
			bulletinDaytimeDescription = this.forenoon;

		Element rootElement = doc.createElement("Bulletin");

		// attributes
		if (getId() != null) {
			if (isAfternoon)
				rootElement.setAttribute("gml:id", getId() + "_PM");
			else
				rootElement.setAttribute("gml:id", getId());
		}
		if (languageCode == null)
			languageCode = LanguageCode.en;
		rootElement.setAttribute("xml:lang", languageCode.toString());

		// metaData
		Element metaDataProperty = XmlUtil.createMetaDataProperty(doc, publicationDate, languageCode);
		rootElement.appendChild(metaDataProperty);

		// validTime
		if (validFrom != null && validUntil != null) {

			ZonedDateTime start = validFrom;
			ZonedDateTime end = validUntil;

			if (hasDaytimeDependency) {
				if (isAfternoon)
					start = start.plusHours(12);
				else
					end = end.minusHours(12);
			}

			Element validTime = doc.createElement("validTime");
			Element timePeriod = doc.createElement("TimePeriod");
			Element beginPosition = doc.createElement("beginPosition");
			beginPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(start)));
			timePeriod.appendChild(beginPosition);
			Element endPosition = doc.createElement("endPosition");
			endPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(end)));
			timePeriod.appendChild(endPosition);
			validTime.appendChild(timePeriod);
			rootElement.appendChild(validTime);
		}

		// srcRef
		Element srcRef = doc.createElement("srcRef");
		Element operation = doc.createElement("Operation");
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode("Avalanche.report"));
		operation.appendChild(name);
		srcRef.appendChild(operation);
		rootElement.appendChild(srcRef);

		// locRef
		for (String publishedRegion : publishedRegions) {
			if (region.affects(publishedRegion)) {
				Element locRef = doc.createElement("locRef");
				locRef.setAttribute("xlink:href", publishedRegion);
				rootElement.appendChild(locRef);
			}
		}

		// bulletinResultsOf
		Element bulletinResultsOf = doc.createElement("bulletinResultsOf");
		Element bulletinMeasurements = doc.createElement("BulletinMeasurements");

		// danger ratings
		Element dangerRatings = doc.createElement("dangerRatings");
		if (bulletinDaytimeDescription.isHasElevationDependency()) {
			Element dangerRatingAbove = doc.createElement("DangerRating");
			Element validElevationAbove = doc.createElement("validElevation");
			validElevationAbove.setAttribute("xlink:href", XmlUtil.createValidElevationAttribute(
					bulletinDaytimeDescription.getElevation(), true, bulletinDaytimeDescription.getTreeline()));
			dangerRatingAbove.appendChild(validElevationAbove);

			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingAbove() != null) {
				Element mainValueAbove = doc.createElement("mainValue");
				mainValueAbove.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv5String(bulletinDaytimeDescription.getDangerRatingAbove())));
				dangerRatingAbove.appendChild(mainValueAbove);
			}
			dangerRatings.appendChild(dangerRatingAbove);
			Element dangerRatingBelow = doc.createElement("DangerRating");
			Element validElevationBelow = doc.createElement("validElevation");
			validElevationBelow.setAttribute("xlink:href", XmlUtil.createValidElevationAttribute(
					bulletinDaytimeDescription.getElevation(), false, bulletinDaytimeDescription.getTreeline()));
			dangerRatingBelow.appendChild(validElevationBelow);

			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingBelow() != null) {
				Element mainValueBelow = doc.createElement("mainValue");
				mainValueBelow.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv5String(bulletinDaytimeDescription.getDangerRatingBelow())));
				dangerRatingBelow.appendChild(mainValueBelow);
			}
			dangerRatings.appendChild(dangerRatingBelow);
		} else {
			// NOTE if no elevation dependency is set, the elevation description is above
			Element dangerRating = doc.createElement("DangerRating");

			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingAbove() != null) {
				Element mainValue = doc.createElement("mainValue");
				mainValue.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv5String(bulletinDaytimeDescription.getDangerRatingAbove())));
				dangerRating.appendChild(mainValue);
			}
			dangerRatings.appendChild(dangerRating);
		}
		bulletinMeasurements.appendChild(dangerRatings);

		// danger patterns
		if (dangerPattern1 != null || dangerPattern2 != null) {
			Element dangerPatterns = doc.createElement("dangerPatterns");
			if (dangerPattern1 != null) {
				Element dangerPatternOne = doc.createElement("DangerPattern");
				Element dangerPatternOneType = doc.createElement("type");
				dangerPatternOneType.appendChild(doc.createTextNode(DangerPattern.getCAAMLv5String(dangerPattern1)));
				dangerPatternOne.appendChild(dangerPatternOneType);
				dangerPatterns.appendChild(dangerPatternOne);
			}
			if (dangerPattern2 != null) {
				Element dangerPatternTwo = doc.createElement("DangerPattern");
				Element dangerPatternTwoType = doc.createElement("type");
				dangerPatternTwoType.appendChild(doc.createTextNode(DangerPattern.getCAAMLv5String(dangerPattern2)));
				dangerPatternTwo.appendChild(dangerPatternTwoType);
				dangerPatterns.appendChild(dangerPatternTwo);
			}
			bulletinMeasurements.appendChild(dangerPatterns);
		}

		// avalanche problems
		Element avProblems = doc.createElement("avProblems");
		for (AvalancheSituation situation : bulletinDaytimeDescription != null
				? bulletinDaytimeDescription.getAvalancheSituations()
				: Collections.<AvalancheSituation>emptyList()) {
			if (situation != null && situation.getAvalancheSituation() != null) {
				Element avProblem = getAvProblemCaamlv5(doc, situation, languageCode);
				avProblems.appendChild(avProblem);
			}
		}
		bulletinMeasurements.appendChild(avProblems);

		// tendency
		if (this.getTendency() != null) {
			Element tendencyElement = doc.createElement("tendency");
			Element type = doc.createElement("type");
			type.appendChild(doc.createTextNode(Tendency.getCaamlString(this.getTendency())));
			tendencyElement.appendChild(type);

			if (validFrom != null && validUntil != null) {
				ZonedDateTime start = validFrom;
				ZonedDateTime end = validUntil;

				start = start.plusDays(1);
				end = end.plusDays(1);

				Element validTime = doc.createElement("validTime");
				Element timePeriod = doc.createElement("TimePeriod");
				Element beginPosition = doc.createElement("beginPosition");
				beginPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(start)));
				timePeriod.appendChild(beginPosition);
				Element endPosition = doc.createElement("endPosition");
				endPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(end)));
				timePeriod.appendChild(endPosition);
				validTime.appendChild(timePeriod);
				tendencyElement.appendChild(validTime);
			}
			bulletinMeasurements.appendChild(tendencyElement);
		}

		for (TextPart part : TextPart.values()) {
			if (textPartsMap.get(part) != null && textPartsMap.get(part).getTexts() != null
					&& (!textPartsMap.get(part).getTexts().isEmpty())
					&& (textPartsMap.get(part).getText(languageCode) != null)) {
				Element textPart = doc.createElement(part.toCaamlv5String());
				textPart.appendChild(doc.createTextNode(textPartsMap.get(part).getText(languageCode)));
				bulletinMeasurements.appendChild(textPart);
			}
		}

		bulletinResultsOf.appendChild(bulletinMeasurements);
		rootElement.appendChild(bulletinResultsOf);

		return rootElement;
	}

	private Element createCAAMLv6Bulletin(Document doc, LanguageCode languageCode, Region region, boolean isAfternoon, String reportPublicationTime, ServerInstance serverInstance) {

		AvalancheBulletinDaytimeDescription bulletinDaytimeDescription;

		if (isAfternoon)
			bulletinDaytimeDescription = this.afternoon;
		else
			bulletinDaytimeDescription = this.forenoon;

		Element rootElement = doc.createElement("bulletin");

		// attributes
		if (getId() != null) {
			if (isAfternoon)
				rootElement.setAttribute("id", getId() + "_PM");
			else
				rootElement.setAttribute("id", getId());
		}
		if (languageCode == null)
			languageCode = LanguageCode.en;
		rootElement.setAttribute("lang", languageCode.toString());

		// metaData
		Element metaData = doc.createElement("metaData");
		rootElement.appendChild(metaData);
		if (!isAfternoon) {
			// TODO use specific file for region
			String fileReferenceURI = LinkUtil.getMapsUrl(languageCode, region, serverInstance) + "/" + getValidityDateString() + "/"
					+ reportPublicationTime + "/" + getId() + ".jpg";
			metaData.appendChild(XmlUtil.createExtFile(doc, "dangerRatingMap",
					languageCode.getBundleString("ext-file.thumbnail.description"), fileReferenceURI));
		} else {
			// TODO use specific file for region
			String fileReferenceURI = LinkUtil.getMapsUrl(languageCode, region, serverInstance) + "/" + getValidityDateString() + "/"
					+ reportPublicationTime + "/" + getId() + "_PM.jpg";
			metaData.appendChild(XmlUtil.createExtFile(doc, "dangerRatingMap",
					languageCode.getBundleString("ext-file.thumbnail.description"), fileReferenceURI));
		}
		String linkReferenceURI = languageCode.getBundleString("website.url") + "/bulletin/" + getValidityDateString()
				+ "?region=" + getId();
		metaData.appendChild(XmlUtil.createExtFile(doc, "website",
				languageCode.getBundleString("ext-file.region-link.description"), linkReferenceURI));

		// publication time
		Element pubTime = doc.createElement("publicationTime");
		pubTime.appendChild(doc.createTextNode(
				DateTimeFormatter.ISO_INSTANT.format(publicationDate)));
		rootElement.appendChild(pubTime);

		// validTime
		if (validFrom != null && validUntil != null) {

			ZonedDateTime start = validFrom;
			ZonedDateTime end = validUntil;

			if (hasDaytimeDependency) {
				if (isAfternoon)
					start = start.plusHours(12);
				else
					end = end.minusHours(12);
			}

			Element validTime = doc.createElement("validTime");
			Element beginPosition = doc.createElement("startTime");
			beginPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(start)));
			validTime.appendChild(beginPosition);
			Element endPosition = doc.createElement("endTime");
			endPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(end)));
			validTime.appendChild(endPosition);
			rootElement.appendChild(validTime);
		}

		// source
		Element source = doc.createElement("source");
		Element operation = doc.createElement("operation");
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(languageCode.getBundleString("website.name")));
		operation.appendChild(name);
		Element website = doc.createElement("website");
		website.appendChild(doc.createTextNode(languageCode.getBundleString("website.url")));
		operation.appendChild(website);
		source.appendChild(operation);
		rootElement.appendChild(source);

		// region
		for (String regionId : publishedRegions) {
			if (region.affects(regionId)) {
				Element regionElement = doc.createElement("region");
				// Element nameElement = doc.createElement("name");
				// nameElement.appendChild(doc.createTextNode(RegionController.getInstance().getRegionName(languageCode, regionId)));
				// regionElement.appendChild(nameElement);
				regionElement.setAttribute("id", regionId);
				rootElement.appendChild(regionElement);
			}
		}

		// complexity
		if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getComplexity() != null) {
			// Element complexity = doc.createElement("complexity");
			// complexity.appendChild(doc.createTextNode(Complexity.getCAAMLString(bulletin.getComplexity())));
			// rootElement.appendChild(complexity);
			if (bulletinDaytimeDescription.getComplexity() == Complexity.complex) {
				rootElement.setAttribute("complex", "true");
			}
		}

		// danger ratings
		if (bulletinDaytimeDescription.isHasElevationDependency()) {
			Element dangerRatingAbove = doc.createElement("dangerRating");
			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingAbove() != null) {
				Element mainValueAbove = doc.createElement("mainValue");
				mainValueAbove.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv6String(bulletinDaytimeDescription.getDangerRatingAbove())));
				dangerRatingAbove.appendChild(mainValueAbove);
			}
			Element elevationAbove = doc.createElement("elevation");
			elevationAbove.setAttribute("uom", "m");
			Element lowerBound = doc.createElement("lowerBound");
			if (bulletinDaytimeDescription.getTreeline())
				lowerBound.appendChild(doc.createTextNode("treeline"));
			else
				lowerBound.appendChild(doc.createTextNode(String.valueOf(bulletinDaytimeDescription.getElevation())));
			elevationAbove.appendChild(lowerBound);
			dangerRatingAbove.appendChild(elevationAbove);
			if (bulletinDaytimeDescription.getMatrixInformationAbove() != null)
				bulletinDaytimeDescription.getMatrixInformationAbove().toCAAMLv6(doc, dangerRatingAbove);
			rootElement.appendChild(dangerRatingAbove);

			Element dangerRatingBelow = doc.createElement("dangerRating");
			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingBelow() != null) {
				Element mainValueBelow = doc.createElement("mainValue");
				mainValueBelow.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv6String(bulletinDaytimeDescription.getDangerRatingBelow())));
				dangerRatingBelow.appendChild(mainValueBelow);
			}
			Element elevationBelow = doc.createElement("elevation");
			elevationBelow.setAttribute("uom", "m");
			Element upperBound = doc.createElement("upperBound");
			if (bulletinDaytimeDescription.getTreeline())
				upperBound.appendChild(doc.createTextNode("treeline"));
			else
				upperBound.appendChild(doc.createTextNode(String.valueOf(bulletinDaytimeDescription.getElevation())));
			elevationBelow.appendChild(upperBound);
			dangerRatingBelow.appendChild(elevationBelow);
			if (bulletinDaytimeDescription.getMatrixInformationBelow() != null)
				bulletinDaytimeDescription.getMatrixInformationBelow().toCAAMLv6(doc, dangerRatingBelow);
			rootElement.appendChild(dangerRatingBelow);
		} else {
			// NOTE if no elevation dependency is set, the elevation description is above
			Element dangerRating = doc.createElement("dangerRating");
			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingAbove() != null) {
				Element mainValue = doc.createElement("mainValue");
				mainValue.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv6String(bulletinDaytimeDescription.getDangerRatingAbove())));
				dangerRating.appendChild(mainValue);
			}
			if (bulletinDaytimeDescription.getMatrixInformationAbove() != null)
				bulletinDaytimeDescription.getMatrixInformationAbove().toCAAMLv6(doc, dangerRating);
			rootElement.appendChild(dangerRating);
		}

		// danger patterns
		if (dangerPattern1 != null) {
			Element dangerPatternOne = doc.createElement("dangerPattern");
			Element dangerPatternOneType = doc.createElement("type");
			dangerPatternOneType.appendChild(doc.createTextNode(DangerPattern.getCAAMLv6String(dangerPattern1)));
			dangerPatternOne.appendChild(dangerPatternOneType);
			rootElement.appendChild(dangerPatternOne);
		}
		if (dangerPattern2 != null) {
			Element dangerPatternTwo = doc.createElement("dangerPattern");
			Element dangerPatternTwoType = doc.createElement("type");
			dangerPatternTwoType.appendChild(doc.createTextNode(DangerPattern.getCAAMLv6String(dangerPattern2)));
			dangerPatternTwo.appendChild(dangerPatternTwoType);
			rootElement.appendChild(dangerPatternTwo);
		}

		// avalanche problems
		for (AvalancheSituation situation : bulletinDaytimeDescription != null
				? bulletinDaytimeDescription.getAvalancheSituations()
				: Collections.<AvalancheSituation>emptyList()) {
			if (situation != null && situation.getAvalancheSituation() != null) {
				Element avalancheProblem = getAvalancheProblemCaamlv6(doc, situation, languageCode);
				rootElement.appendChild(avalancheProblem);
			}
		}

		// tendency
		if (this.getTendency() != null) {
			Element tendencyElement = doc.createElement("tendency");
			Element type = doc.createElement("type");
			type.appendChild(doc.createTextNode(Tendency.getCaamlString(this.getTendency())));
			tendencyElement.appendChild(type);

			if (validFrom != null && validUntil != null) {
				ZonedDateTime start = validFrom;
				ZonedDateTime end = validUntil;

				start = start.plusDays(1);
				end = end.plusDays(1);

				Element validTime = doc.createElement("validTime");
				Element startTime = doc.createElement("startTime");
				startTime.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(start)));
				validTime.appendChild(startTime);
				Element endTime = doc.createElement("endTime");
				endTime.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(end)));
				validTime.appendChild(endTime);
				tendencyElement.appendChild(validTime);
			}
			rootElement.appendChild(tendencyElement);
		}

		for (TextPart part : TextPart.values()) {
			if (textPartsMap.get(part) != null && textPartsMap.get(part).getTexts() != null
					&& (!textPartsMap.get(part).getTexts().isEmpty())
					&& (textPartsMap.get(part).getText(languageCode) != null)) {
				Element textPart = doc.createElement(part.toCaamlv6String());
				textPart.appendChild(doc.createTextNode(textPartsMap.get(part).getText(languageCode).trim()));
				rootElement.appendChild(textPart);
			}
		}

		return rootElement;
	}

	private Element getAvProblemCaamlv5(Document doc, AvalancheSituation avalancheSituation,
			LanguageCode languageCode) {
		Element avProblem = doc.createElement("AvProblem");
		Element type = doc.createElement("type");
		type.appendChild(doc.createTextNode(avalancheSituation.getAvalancheSituation().toCaamlv5String()));
		avProblem.appendChild(type);
		if (avalancheSituation.getAspects() != null) {
			for (Aspect aspect : avalancheSituation.getAspects()) {
				Element validAspect = doc.createElement("validAspect");
				validAspect.setAttribute("xlink:href", aspect.toCaamlString());
				avProblem.appendChild(validAspect);
			}
		}

		if (avalancheSituation.getTreelineHigh() || avalancheSituation.getElevationHigh() > 0) {
			if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
				// elevation high and low set
				Element validElevation = doc.createElement("validElevation");
				Element elevationRange = doc.createElement("elevationRange");
				elevationRange.setAttribute("uom", "m");
				Element beginPosition = doc.createElement("beginPosition");
				if (avalancheSituation.getTreelineLow())
					beginPosition.appendChild(doc.createTextNode("Treeline"));
				else
					beginPosition.appendChild(doc.createTextNode(String.valueOf(avalancheSituation.getElevationLow())));
				Element endPosition = doc.createElement("endPosition");
				if (avalancheSituation.getTreelineHigh())
					endPosition.appendChild(doc.createTextNode("Treeline"));
				else
					endPosition.appendChild(doc.createTextNode(String.valueOf(avalancheSituation.getElevationHigh())));
				elevationRange.appendChild(beginPosition);
				elevationRange.appendChild(endPosition);
				validElevation.appendChild(elevationRange);
				avProblem.appendChild(validElevation);
			} else {
				// elevation high set
				Element validElevation = doc.createElement("validElevation");
				String elevationString;
				if (avalancheSituation.getTreelineHigh())
					elevationString = XmlUtil.createValidElevationAttribute(0, false, true);
				else
					elevationString = XmlUtil.createValidElevationAttribute(avalancheSituation.getElevationHigh(),
							false, false);
				validElevation.setAttribute("xlink:href", elevationString);
				avProblem.appendChild(validElevation);
			}
		} else if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
			// elevation low set
			Element validElevation = doc.createElement("validElevation");
			String elevationString;
			if (avalancheSituation.getTreelineLow())
				elevationString = XmlUtil.createValidElevationAttribute(0, true, true);
			else
				elevationString = XmlUtil.createValidElevationAttribute(avalancheSituation.getElevationLow(), true,
						false);
			validElevation.setAttribute("xlink:href", elevationString);
			avProblem.appendChild(validElevation);
		} else {
			// no elevation set
		}

		return avProblem;
	}

	private Element getAvalancheProblemCaamlv6(Document doc, AvalancheSituation avalancheSituation,
			LanguageCode languageCode) {
		Element avalancheProblem = doc.createElement("avalancheProblem");
		Element type = doc.createElement("type");
		type.appendChild(doc.createTextNode(avalancheSituation.getAvalancheSituation().toCaamlv6String()));
		avalancheProblem.appendChild(type);
		Element dangerRating = doc.createElement("dangerRating");
		if (avalancheSituation.getAspects() != null) {
			for (Aspect aspect : avalancheSituation.getAspects()) {
				Element validAspect = doc.createElement("aspect");
				validAspect.appendChild(doc.createTextNode(aspect.toUpperCaseString()));
				dangerRating.appendChild(validAspect);
			}
		}

		if (avalancheSituation.getTreelineHigh() || avalancheSituation.getElevationHigh() > 0) {
			Element validElevation = doc.createElement("elevation");
			validElevation.setAttribute("uom", "m");
			if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
				// elevation high and low set
				Element lowerBound = doc.createElement("lowerBound");
				if (avalancheSituation.getTreelineLow())
					lowerBound.appendChild(doc.createTextNode("treeline"));
				else
					lowerBound.appendChild(doc.createTextNode(String.valueOf(avalancheSituation.getElevationLow())));
				Element upperBound = doc.createElement("upperBound");
				if (avalancheSituation.getTreelineHigh())
					upperBound.appendChild(doc.createTextNode("treeline"));
				else
					upperBound.appendChild(doc.createTextNode(String.valueOf(avalancheSituation.getElevationHigh())));
				validElevation.appendChild(lowerBound);
				validElevation.appendChild(upperBound);
			} else {
				// elevation high set
				Element upperBound = doc.createElement("upperBound");
				if (avalancheSituation.getTreelineHigh())
					upperBound.appendChild(doc.createTextNode("treeline"));
				else
					upperBound.appendChild(doc.createTextNode(String.valueOf(avalancheSituation.getElevationHigh())));
				validElevation.appendChild(upperBound);
			}
			dangerRating.appendChild(validElevation);
		} else if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
			// elevation low set
			Element validElevation = doc.createElement("elevation");
			validElevation.setAttribute("uom", "m");
			if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
				Element lowerBound = doc.createElement("lowerBound");
				if (avalancheSituation.getTreelineLow())
					lowerBound.appendChild(doc.createTextNode("treeline"));
				else
					lowerBound.appendChild(doc.createTextNode(String.valueOf(avalancheSituation.getElevationLow())));
				validElevation.appendChild(lowerBound);
			}
			dangerRating.appendChild(validElevation);
		} else {
			// no elevation set
		}

		// terrain feature
		if (avalancheSituation.getTerrainFeature() != null && !avalancheSituation.getTerrainFeature().isEmpty()
				&& avalancheSituation.getTerrainFeature(languageCode) != null) {
			Element textPart = doc.createElement("terrainFeature");
			textPart.appendChild(doc.createTextNode(avalancheSituation.getTerrainFeature(languageCode)));
			dangerRating.appendChild(textPart);
		}

		// danger rating
		if (avalancheSituation.getMatrixInformation() != null) {
			DangerRating rating = null;
			if (avalancheSituation.getMatrixInformation().getArtificialDangerRating() != null) {
				if (avalancheSituation.getMatrixInformation().getNaturalDangerRating() != null) {
					if (avalancheSituation.getMatrixInformation().getArtificialDangerRating()
							.compareTo(avalancheSituation.getMatrixInformation().getNaturalDangerRating()) < 0)
						rating = avalancheSituation.getMatrixInformation().getNaturalDangerRating();
					else
						rating = avalancheSituation.getMatrixInformation().getArtificialDangerRating();
				} else {
					rating = avalancheSituation.getMatrixInformation().getArtificialDangerRating();
				}
			} else if (avalancheSituation.getMatrixInformation().getNaturalDangerRating() != null) {
				rating = avalancheSituation.getMatrixInformation().getNaturalDangerRating();
			}
			if (rating != null) {
				Element mainValue = doc.createElement("mainValue");
				mainValue.appendChild(doc.createTextNode(DangerRating.getCAAMLv6String(rating)));
				dangerRating.appendChild(mainValue);
			}

			avalancheSituation.getMatrixInformation().toCAAMLv6(doc, dangerRating);
		}

		avalancheProblem.appendChild(dangerRating);

		return avalancheProblem;
	}

	public List<Element> toCAAMLv5(Document doc, LanguageCode languageCode, Region region) {
		if (publishedRegions != null && !publishedRegions.isEmpty()) {
			List<Element> result = new ArrayList<Element>();
			result.add(createCAAMLv5Bulletin(doc, languageCode, region, false));

			if (hasDaytimeDependency)
				result.add(createCAAMLv5Bulletin(doc, languageCode, region, true));

			return result;
		} else
			return null;
	}

	public List<Element> toCAAMLv6(Document doc, LanguageCode languageCode, Region region, String reportPublicationTime, ServerInstance serverInstance) {
		if (publishedRegions != null && !publishedRegions.isEmpty()) {
			List<Element> result = new ArrayList<Element>();
			result.add(createCAAMLv6Bulletin(doc, languageCode, region, false, reportPublicationTime, serverInstance));

			if (hasDaytimeDependency)
				result.add(createCAAMLv6Bulletin(doc, languageCode, region, true, reportPublicationTime, serverInstance));

			return result;
		} else
			return null;
	}

	public void copy(AvalancheBulletin bulletin) {
		setUser(bulletin.getUser());
		setAdditionalAuthors(bulletin.getAdditionalAuthors());
		setPublicationDate(bulletin.getPublicationDate());
		setValidFrom(bulletin.getValidFrom());
		setValidUntil(bulletin.getValidUntil());
		setSuggestedRegions(bulletin.getSuggestedRegions());
		setPublishedRegions(bulletin.getPublishedRegions());
		setSavedRegions(bulletin.getSavedRegions());
		setHasDaytimeDependency(bulletin.isHasDaytimeDependency());
		setTendency(bulletin.getTendency());
		setDangerPattern1(bulletin.getDangerPattern1());
		setDangerPattern2(bulletin.getDangerPattern2());

		if (bulletin.getForenoon() != null) {
			if (forenoon == null)
				forenoon = bulletin.getForenoon();
			else {
				forenoon.setHasElevationDependency(bulletin.getForenoon().isHasElevationDependency());
				forenoon.setTreeline(bulletin.getForenoon().getTreeline());
				forenoon.setElevation(bulletin.getForenoon().getElevation());
				forenoon.setDangerRatingAbove(bulletin.getForenoon().getDangerRatingAbove());
				forenoon.setMatrixInformationAbove(bulletin.getForenoon().getMatrixInformationAbove());
				forenoon.setTerrainFeatureAboveTextcat(bulletin.getForenoon().getTerrainFeatureAboveTextcat());
				forenoon.setTerrainFeatureAbove(bulletin.getForenoon().getTerrainFeatureAbove());
				forenoon.setDangerRatingBelow(bulletin.getForenoon().getDangerRatingBelow());
				forenoon.setMatrixInformationBelow(bulletin.getForenoon().getMatrixInformationBelow());
				forenoon.setTerrainFeatureBelowTextcat(bulletin.getForenoon().getTerrainFeatureBelowTextcat());
				forenoon.setTerrainFeatureBelow(bulletin.getForenoon().getTerrainFeatureBelow());
				forenoon.setComplexity(bulletin.getForenoon().getComplexity());
				forenoon.setAvalancheSituation1(bulletin.getForenoon().getAvalancheSituation1());
				forenoon.setAvalancheSituation2(bulletin.getForenoon().getAvalancheSituation2());
				forenoon.setAvalancheSituation3(bulletin.getForenoon().getAvalancheSituation3());
				forenoon.setAvalancheSituation4(bulletin.getForenoon().getAvalancheSituation4());
				forenoon.setAvalancheSituation5(bulletin.getForenoon().getAvalancheSituation5());
			}
		}

		if (bulletin.getAfternoon() != null) {
			if (afternoon == null)
				afternoon = bulletin.getAfternoon();
			else {
				afternoon.setHasElevationDependency(bulletin.getAfternoon().isHasElevationDependency());
				afternoon.setTreeline(bulletin.getAfternoon().getTreeline());
				afternoon.setElevation(bulletin.getAfternoon().getElevation());
				afternoon.setDangerRatingAbove(bulletin.getAfternoon().getDangerRatingAbove());
				afternoon.setMatrixInformationAbove(bulletin.getAfternoon().getMatrixInformationAbove());
				afternoon.setTerrainFeatureAboveTextcat(bulletin.getAfternoon().getTerrainFeatureAboveTextcat());
				afternoon.setTerrainFeatureAbove(bulletin.getAfternoon().getTerrainFeatureAbove());
				afternoon.setDangerRatingBelow(bulletin.getAfternoon().getDangerRatingBelow());
				afternoon.setMatrixInformationBelow(bulletin.getAfternoon().getMatrixInformationBelow());
				afternoon.setTerrainFeatureBelowTextcat(bulletin.getAfternoon().getTerrainFeatureBelowTextcat());
				afternoon.setTerrainFeatureBelow(bulletin.getAfternoon().getTerrainFeatureBelow());
				afternoon.setComplexity(bulletin.getAfternoon().getComplexity());
				afternoon.setAvalancheSituation1(bulletin.getAfternoon().getAvalancheSituation1());
				afternoon.setAvalancheSituation2(bulletin.getAfternoon().getAvalancheSituation2());
				afternoon.setAvalancheSituation3(bulletin.getAfternoon().getAvalancheSituation3());
				afternoon.setAvalancheSituation4(bulletin.getAfternoon().getAvalancheSituation4());
				afternoon.setAvalancheSituation5(bulletin.getAfternoon().getAvalancheSituation5());
			}
		}

		textPartsMap = bulletin.textPartsMap;

		setAvActivityHighlightsTextcat(bulletin.getAvActivityHighlightsTextcat());
		setAvActivityCommentTextcat(bulletin.getAvActivityCommentTextcat());
		setSnowpackStructureHighlightsTextcat(bulletin.getSnowpackStructureHighlightsTextcat());
		setSnowpackStructureCommentTextcat(bulletin.getSnowpackStructureCommentTextcat());
		setTendencyCommentTextcat(bulletin.getTendencyCommentTextcat());

		setAvActivityHighlightsNotes(bulletin.getAvActivityHighlightsNotes());
		setAvActivityCommentNotes(bulletin.getAvActivityCommentNotes());
		setSnowpackStructureHighlightsNotes(bulletin.getSnowpackStructureHighlightsNotes());
		setSnowpackStructureCommentNotes(bulletin.getSnowpackStructureCommentNotes());
		setTendencyCommentNotes(bulletin.getTendencyCommentNotes());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!AvalancheBulletin.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final AvalancheBulletin other = (AvalancheBulletin) obj;

		if (!Objects.equals(this.validFrom, other.validFrom))
			return false;
		if (!Objects.equals(this.validUntil, other.validUntil))
			return false;
		if (this.hasDaytimeDependency != other.hasDaytimeDependency)
			return false;
		if (!Objects.equals(this.forenoon, other.forenoon))
			return false;
		if (!Objects.equals(this.afternoon, other.afternoon))
			return false;
		if (!Objects.equals(this.highlightsTextcat, other.highlightsTextcat))
			return false;
		if (!Objects.equals(this.avActivityHighlightsTextcat, other.avActivityHighlightsTextcat))
			return false;
		if (!Objects.equals(this.avActivityCommentTextcat, other.avActivityCommentTextcat))
			return false;
		if (!Objects.equals(this.snowpackStructureHighlightsTextcat, other.snowpackStructureHighlightsTextcat))
			return false;
		if (!Objects.equals(this.snowpackStructureCommentTextcat, other.snowpackStructureCommentTextcat))
			return false;
		if (!Objects.equals(this.tendencyCommentTextcat, other.tendencyCommentTextcat))
			return false;
		if (!Objects.equals(this.tendency, other.tendency))
			return false;
		if (!Objects.equals(this.dangerPattern1, other.dangerPattern1))
			return false;
		if (!Objects.equals(this.dangerPattern2, other.dangerPattern2))
			return false;

		return true;
	}

	/**
	 * Sort {@code AvalancheBulletin} by highest danger rating (descending) with
	 * regard to daytime and elevation dependency.
	 *
	 */
	@Override
	public int compareTo(AvalancheBulletin other) {
		return Integer.compare(other.getHighestDangerRatingDouble(), getHighestDangerRatingDouble());
	}

	public static AvalancheBulletin readBulletin(final URL resource) throws IOException {
		final String validBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		return new AvalancheBulletin(new JSONObject(validBulletinStringFromResource), User::new);
	}

	public static List<AvalancheBulletin> readBulletins(final URL resource) throws IOException {
		final String validBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		final JSONArray array = new JSONArray(validBulletinStringFromResource);
		return IntStream.range(0, array.length()).mapToObj(array::getJSONObject).map(u -> new AvalancheBulletin(u, User::new))
				.collect(Collectors.toList());
	}

}
