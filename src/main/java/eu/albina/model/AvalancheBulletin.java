// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.albina.jobs.PublicationStrategy;
import eu.albina.util.JsonUtil;

import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.StrategicMindset;
import eu.albina.model.enumerations.Tendency;
import eu.albina.model.enumerations.TextPart;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * This class holds all information about one avalanche bulletin.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "avalanche_bulletins")
@Serdeable
@JsonView({JsonUtil.Views.Public.class, JsonUtil.Views.Internal.class})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Introspected(excludedAnnotations = {JsonIgnore.class})
public class AvalancheBulletin extends AbstractPersistentObject
		implements Comparable<AvalancheBulletin>, HasValidityDate, HasPublicationDate {

	/** Information about the author of the avalanche bulletin */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID")
	@JsonProperty("author")
	@JsonSerialize(as = NameAndEmail.class)
	private User user;

	@Column(name = "OWNER_REGION", length = 191)
	private String ownerRegion;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "avalanche_bulletin_additional_users", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "ADDITIONAL_USER_NAME", length = 191)
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private Set<String> additionalAuthors;

	@Column(name = "SAVE_DATE")
	@DateUpdated
	private ZonedDateTime saveDate;

	@Column(name = "PUBLICATION_DATE")
	private ZonedDateTime publicationDate;

	/** Validity of the avalanche bulletin */
	@Column(name = "VALID_FROM")
	@JsonIgnore
	private ZonedDateTime validFrom;

	@Column(name = "VALID_UNTIL")
	@JsonIgnore
	private ZonedDateTime validUntil;

	/** The recommended regions the avalanche bulletin is for. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "avalanche_bulletin_suggested_regions", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID", length = 191)
	@JsonView(JsonUtil.Views.Internal.class)
	private Set<String> suggestedRegions;

	/** The published regions the avalanche bulletin is for. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "avalanche_bulletin_published_regions", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID",  length = 191)
	@JsonView(JsonUtil.Views.Internal.class)
	private Set<String> publishedRegions;

	/** The saved regions the avalanche bulletin is for. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "avalanche_bulletin_saved_regions", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ID"))
	@Column(name = "REGION_ID", length = 191)
	@JsonView(JsonUtil.Views.Internal.class)
	private Set<String> savedRegions;

	@Enumerated(EnumType.STRING)
	@Column(name = "STRATEGIC_MINDSET", length=191)
	private StrategicMindset strategicMindset;

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
	@Column(name = "GENERAL_HEADLINE_COMMENT_TEXTCAT")
	private String generalHeadlineCommentTextcat;

	@Lob
	@Column(name = "SYNOPSIS_COMMENT_TEXTCAT")
	private String synopsisCommentTextcat;

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

	@Lob
	@Column(name = "GENERAL_HEADLINE_COMMENT_NOTES")
	private String generalHeadlineCommentNotes;

	@Enumerated(EnumType.STRING)
	@Column(name = "TENDENCY", length=191)
	private Tendency tendency;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_PATTERN_1", length = 191)
	private DangerPattern dangerPattern1;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_PATTERN_2", length = 191)
	private DangerPattern dangerPattern2;

	/** Collection containing all text parts available for a bulletin */
	@OneToMany(mappedBy = "avalancheBulletin", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JsonIgnore
	private Collection<AvalancheBulletinText> textPartsMap;

	/**
	 * Standard constructor for an avalanche bulletin.
	 */
	public AvalancheBulletin() {
		additionalAuthors = new LinkedHashSet<>();
		textPartsMap = new ArrayList<>();
		publishedRegions = new LinkedHashSet<>();
		savedRegions = new LinkedHashSet<>();
		suggestedRegions = new LinkedHashSet<>();
	}

	public AvalancheBulletin withRegionFilter(Region region) {
		AvalancheBulletin b = new AvalancheBulletin();
		b.copy(this);
		b.setId(getId());
		b.getPublishedRegions().removeIf(region::isForeign);
		b.getSavedRegions().removeIf(region::isForeign);
		b.getSuggestedRegions().removeIf(region::isForeign);
		return b;
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

	public ZonedDateTime getSaveDate() {
		return saveDate;
	}

	public void setSaveDate(ZonedDateTime saveDate) {
		this.saveDate = saveDate;
	}

	public String getOwnerRegion() {
		return ownerRegion;
	}

	public void setOwnerRegion(String ownerRegion) {
		this.ownerRegion = ownerRegion;
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

	public String getGeneralHeadlineCommentTextcat() {
		return generalHeadlineCommentTextcat;
	}

	public void setGeneralHeadlineCommentTextcat(String generalHeadlineCommentTextcat) {
		this.generalHeadlineCommentTextcat = generalHeadlineCommentTextcat;
	}

	public void setSynopsisCommentTextcat(String synopsisCommentTextcat) {
		this.synopsisCommentTextcat = synopsisCommentTextcat;
	}

	public String getSynopsisCommentTextcat() {
		return synopsisCommentTextcat;
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

	public String getGeneralHeadlineCommentNotes() {
		return generalHeadlineCommentNotes;
	}

	public void setGeneralHeadlineCommentNotes(String generalHeadlineCommentNotes) {
		this.generalHeadlineCommentNotes = generalHeadlineCommentNotes;
	}

	@Serdeable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	record Text(LanguageCode languageCode, String text) implements Comparable<Text> {

		private static final Comparator<Text> COMPARATOR = Comparator.comparing(Text::languageCode);

		@Override
		public int compareTo(Text o) {
			return COMPARATOR.compare(this, o);
		}
	}

	private Set<Text> getTexts(TextPart textPart) {
		return textPartsMap.stream()
			.filter(p -> p.getId().getTextType() == textPart)
			.map(p -> new Text(p.getId().getLanguageCode(), p.getText()))
			.collect(Collectors.toCollection(TreeSet::new));
	}

	private void putTexts(TextPart textPart, Set<Text> texts) {
		textPartsMap.removeIf(p -> p.getId().getTextType() == textPart);
		for (Text text : texts) {
			AvalancheBulletinText.AvalancheBulletinTextId id = new AvalancheBulletinText.AvalancheBulletinTextId();
			id.setLanguageCode(text.languageCode());
			id.setTextType(textPart);
			AvalancheBulletinText t = new AvalancheBulletinText();
			t.setId(id);
			t.setAvalancheBulletin(this);
			t.setText(text.text());
			textPartsMap.add(t);
		}
	}

	public Set<Text> getHighlights() {
		return getTexts(TextPart.highlights);
	}

	public String getTextPartIn(TextPart textPart, LanguageCode lang) {
		Set<Text> texts = getTexts(textPart);
		return texts.stream().filter(t -> t.languageCode() == lang).findFirst()
			.map(Text::text)
			.map(String::trim)
			.filter(s -> !s.isBlank()).orElse(null);
	}

	public String getHighlightsIn(LanguageCode lang) {
		return getTextPartIn(TextPart.highlights, lang);
	}

	public void setHighlights(Set<Text> highlights) {
		putTexts(TextPart.highlights, highlights);
	}

	public Set<Text> getAvActivityHighlights() {
		return getTexts(TextPart.avActivityHighlights);
	}

	public String getAvActivityHighlightsIn(LanguageCode lang) {
		return getTextPartIn(TextPart.avActivityHighlights, lang);
	}

	public void setAvActivityHighlights(Set<Text> avActivityHighlights) {
		putTexts(TextPart.avActivityHighlights, avActivityHighlights);
	}

	public Set<Text> getAvActivityComment() {
		return getTexts(TextPart.avActivityComment);
	}

	public String getAvActivityCommentIn(LanguageCode lang) {
		return getTextPartIn(TextPart.avActivityComment, lang);
	}

	public void setAvActivityComment(Set<Text> avActivityComment) {
		putTexts(TextPart.avActivityComment, avActivityComment);
	}

	public Set<Text> getSynopsisHighlights() {
		return getTexts(TextPart.synopsisHighlights);
	}

	public String getSynopsisHighlightsIn(LanguageCode lang) {
		return getTextPartIn(TextPart.synopsisHighlights, lang);
	}

	public void setSynopsisHighlights(Set<Text> synopsisHighlights) {
		putTexts(TextPart.synopsisHighlights, synopsisHighlights);
	}

	public Set<Text> getSynopsisComment() {
		return getTexts(TextPart.synopsisComment);
	}

	public String getSynopsisCommentIn(LanguageCode lang) {
		return getTextPartIn(TextPart.synopsisComment, lang);
	}

	public void setSynopsisComment(Set<Text> synopsisComment) {
		putTexts(TextPart.synopsisComment, synopsisComment);
	}

	public Set<Text> getSnowpackStructureHighlights() {
		return getTexts(TextPart.snowpackStructureHighlights);
	}

	public String getSnowpackStructureHighlightsIn(LanguageCode lang) {
		return getTextPartIn(TextPart.snowpackStructureHighlights, lang);
	}

	public void setSnowpackStructureHighlights(Set<Text> snowpackStructureHighlights) {
		putTexts(TextPart.snowpackStructureHighlights, snowpackStructureHighlights);
	}

	public Set<Text> getSnowpackStructureComment() {
		return getTexts(TextPart.snowpackStructureComment);
	}

	public String getSnowpackStructureCommentIn(LanguageCode lang) {
		return getTextPartIn(TextPart.snowpackStructureComment, lang);
	}

	public void setSnowpackStructureComment(Set<Text> snowpackStructureComment) {
		putTexts(TextPart.snowpackStructureComment, snowpackStructureComment);
	}

	public Set<Text> getTravelAdvisoryHighlights() {
		return getTexts(TextPart.travelAdvisoryHighlights);
	}

	public String getTravelAdvisoryHighlightsIn(LanguageCode lang) {
		return getTextPartIn(TextPart.travelAdvisoryHighlights, lang);
	}

	public void setTravelAdvisoryHighlights(Set<Text> travelAdvisoryHighlights) {
		putTexts(TextPart.travelAdvisoryHighlights, travelAdvisoryHighlights);
	}

	public Set<Text> getTravelAdvisoryComment() {
		return getTexts(TextPart.travelAdvisoryComment);
	}

	public String getTravelAdvisoryCommentIn(LanguageCode lang) {
		return getTextPartIn(TextPart.travelAdvisoryComment, lang);
	}

	public void setTravelAdvisoryComment(Set<Text> travelAdvisoryComment) {
		putTexts(TextPart.travelAdvisoryComment, travelAdvisoryComment);
	}

	public Set<Text> getTendencyComment() {
		return getTexts(TextPart.tendencyComment);
	}

	public String getTendencyCommentIn(LanguageCode lang) {
		return getTextPartIn(TextPart.tendencyComment, lang);
	}

	public void setTendencyComment(Set<Text> tendencyComment) {
		putTexts(TextPart.tendencyComment, tendencyComment);
	}

	public Set<Text> getGeneralHeadlineComment() {
		return getTexts(TextPart.generalHeadlineComment);
	}

	public String getGeneralHeadlineCommentIn(LanguageCode lang) {
		return getTextPartIn(TextPart.generalHeadlineComment, lang);
	}

	public void setGeneralHeadlineComment(Set<Text> generalHeadlineComment) {
		putTexts(TextPart.generalHeadlineComment, generalHeadlineComment);
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

	@Override
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

	public Validity getValidity() {
		return new Validity(validFrom, validUntil);
	}

	public void setValidity(Validity v) {
		validFrom = v.from;
		validUntil = v.until;
	}

	@Serdeable
	public static class Validity {
		private ZonedDateTime from;
		private ZonedDateTime until;

		public Validity() {
		}

		public Validity(ZonedDateTime from, ZonedDateTime until) {
			this.from = from;
			this.until = until;
		}

		public ZonedDateTime getFrom() {
			return from;
		}

		public void setFrom(ZonedDateTime from) {
			this.from = from;
		}

		public ZonedDateTime getUntil() {
			return until;
		}

		public void setUntil(ZonedDateTime until) {
			this.until = until;
		}
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

	@JsonView(JsonUtil.Views.Public.class)
	public Set<String> getRegions() {
		return publishedRegions;
	}

	@JsonView(JsonUtil.Views.Public.class)
	public void setRegions(Set<String> regions) {
		this.publishedRegions = regions;
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

	public StrategicMindset getStrategicMindset() {
		return strategicMindset;
	}

	public void setStrategicMindset(StrategicMindset strategicMindset) {
		this.strategicMindset = strategicMindset;
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

	@JsonIgnore
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
		return getSuggestedRegions().stream().anyMatch(region::affects)
			|| getSavedRegions().stream().anyMatch(region::affects)
			|| getPublishedRegions().stream().anyMatch(region::affects);
	}

	public boolean affectsRegionWithoutSuggestions(Region region) {
		return getSavedRegions().stream().anyMatch(region::affects)
			|| getPublishedRegions().stream().anyMatch(region::affects);
	}

	public boolean affectsRegionOnlyPublished(Region region) {
		if (getPublishedRegions() == null) {
			return false;
		}
		return getPublishedRegions().stream().anyMatch(region::affects);
	}

	public static DangerRating getHighestDangerRating(List<AvalancheBulletin> bulletins) {
		return bulletins.stream()
			.map(AvalancheBulletin::getHighestDangerRating)
			.filter(Objects::nonNull)
			.max(Comparator.naturalOrder())
			.orElse(DangerRating.missing);
	}

	@JsonIgnore
	public DangerRating getHighestDangerRating() {
		DangerRating result = DangerRating.missing;
		if (forenoon != null && forenoon.dangerRating(true) != null
				&& result.compareTo(forenoon.dangerRating(true)) < 0)
			result = forenoon.dangerRating(true);
		if (forenoon != null && forenoon.dangerRating(false) != null
				&& result.compareTo(forenoon.dangerRating(false)) < 0)
			result = forenoon.dangerRating(false);
		if (hasDaytimeDependency) {
			if (afternoon != null && afternoon.dangerRating(true) != null
					&& result.compareTo(afternoon.dangerRating(true)) < 0)
				result = afternoon.dangerRating(true);
			if (afternoon != null && afternoon.dangerRating(false) != null
					&& result.compareTo(afternoon.dangerRating(false)) < 0)
				result = afternoon.dangerRating(false);
		}
		return result;
	}

	@JsonIgnore
	public int getHighestDangerRatingDouble() {
		int sum = 0;
		if (forenoon != null) {
			if (forenoon.dangerRating(true) != null)
				sum += DangerRating.getInt(forenoon.dangerRating(true));
			if (forenoon.dangerRating(false) != null)
				sum += DangerRating.getInt(forenoon.dangerRating(false));
			else
				sum += DangerRating.getInt(forenoon.dangerRating(true));
		}
		if (afternoon != null) {
			if (afternoon.dangerRating(true) != null)
				sum += DangerRating.getInt(afternoon.dangerRating(true));
			if (afternoon.dangerRating(false) != null)
				sum += DangerRating.getInt(afternoon.dangerRating(false));
			else
				sum += DangerRating.getInt(afternoon.dangerRating(true));
		} else if (forenoon != null) {
			if (forenoon.dangerRating(true) != null)
				sum += DangerRating.getInt(forenoon.dangerRating(true));
			if (forenoon.dangerRating(false) != null)
				sum += DangerRating.getInt(forenoon.dangerRating(false));
			else
				sum += DangerRating.getInt(forenoon.dangerRating(true));
		}

		return sum;
	}

	@Override
	@JsonIgnore
	public LocalDate getValidityDate() {
		ZonedDateTime zonedDateTime = validUntil.withZoneSameInstant(PublicationStrategy.localZone());
		LocalTime localTime = zonedDateTime.toLocalTime();
		if (localTime.equals(LocalTime.of(0, 0))) {
			// used until 2024-05-01
			return zonedDateTime.toLocalDate().minusDays(1);
		} else if (localTime.equals(LocalTime.of(17, 0))) {
			// used starting with 2024-12-01
			return zonedDateTime.toLocalDate();
		} else {
			// unspecified
			return zonedDateTime.toLocalDate();
		}
	}

	public void copy(AvalancheBulletin bulletin) {
		setUser(bulletin.getUser());
		setAdditionalAuthors(bulletin.getAdditionalAuthors() != null ? new LinkedHashSet<>(bulletin.getAdditionalAuthors()) : new LinkedHashSet<>());
		setPublicationDate(bulletin.getPublicationDate());
		setValidFrom(bulletin.getValidFrom());
		setValidUntil(bulletin.getValidUntil());
		setSuggestedRegions(bulletin.getSuggestedRegions() != null ? new LinkedHashSet<>(bulletin.getSuggestedRegions()) : new LinkedHashSet<>());
		setPublishedRegions(bulletin.getPublishedRegions() != null ? new LinkedHashSet<>(bulletin.getPublishedRegions()) : new LinkedHashSet<>());
		setSavedRegions(bulletin.getSavedRegions() != null ? new LinkedHashSet<>(bulletin.getSavedRegions()) : new LinkedHashSet<>());
		setHasDaytimeDependency(bulletin.isHasDaytimeDependency());
		setTendency(bulletin.getTendency());
		setStrategicMindset(bulletin.getStrategicMindset());
		setDangerPattern1(bulletin.getDangerPattern1());
		setDangerPattern2(bulletin.getDangerPattern2());

		if (bulletin.getForenoon() != null) {
			if (forenoon == null)
				forenoon = bulletin.getForenoon();
			else {
				forenoon.setHasElevationDependency(bulletin.getForenoon().isHasElevationDependency());
				forenoon.setTreeline(bulletin.getForenoon().getTreeline());
				forenoon.setElevation(bulletin.getForenoon().getElevation());
				forenoon.setDangerRatingAbove(bulletin.getForenoon().dangerRating(true));
				forenoon.setDangerRatingBelow(bulletin.getForenoon().dangerRating(false));
				forenoon.setComplexity(bulletin.getForenoon().getComplexity());
				forenoon.setAvalancheProblem1(bulletin.getForenoon().getAvalancheProblem1());
				forenoon.setAvalancheProblem2(bulletin.getForenoon().getAvalancheProblem2());
				forenoon.setAvalancheProblem3(bulletin.getForenoon().getAvalancheProblem3());
				forenoon.setAvalancheProblem4(bulletin.getForenoon().getAvalancheProblem4());
				forenoon.setAvalancheProblem5(bulletin.getForenoon().getAvalancheProblem5());
			}
		}

		if (bulletin.getAfternoon() != null) {
			if (afternoon == null)
				afternoon = bulletin.getAfternoon();
			else {
				afternoon.setHasElevationDependency(bulletin.getAfternoon().isHasElevationDependency());
				afternoon.setTreeline(bulletin.getAfternoon().getTreeline());
				afternoon.setElevation(bulletin.getAfternoon().getElevation());
				afternoon.setDangerRatingAbove(bulletin.getAfternoon().dangerRating(true));
				afternoon.setDangerRatingBelow(bulletin.getAfternoon().dangerRating(false));
				afternoon.setComplexity(bulletin.getAfternoon().getComplexity());
				afternoon.setAvalancheProblem1(bulletin.getAfternoon().getAvalancheProblem1());
				afternoon.setAvalancheProblem2(bulletin.getAfternoon().getAvalancheProblem2());
				afternoon.setAvalancheProblem3(bulletin.getAfternoon().getAvalancheProblem3());
				afternoon.setAvalancheProblem4(bulletin.getAfternoon().getAvalancheProblem4());
				afternoon.setAvalancheProblem5(bulletin.getAfternoon().getAvalancheProblem5());
			}
		}

		for (TextPart textPart : TextPart.values()) {
			putTexts(textPart, bulletin.getTexts(textPart));
		}

		setAvActivityHighlightsTextcat(bulletin.getAvActivityHighlightsTextcat());
		setAvActivityCommentTextcat(bulletin.getAvActivityCommentTextcat());
		setSnowpackStructureHighlightsTextcat(bulletin.getSnowpackStructureHighlightsTextcat());
		setSnowpackStructureCommentTextcat(bulletin.getSnowpackStructureCommentTextcat());
		setTendencyCommentTextcat(bulletin.getTendencyCommentTextcat());
		setGeneralHeadlineCommentTextcat(bulletin.getGeneralHeadlineCommentTextcat());
		setSynopsisCommentTextcat(bulletin.getSynopsisCommentTextcat());

		setAvActivityHighlightsNotes(bulletin.getAvActivityHighlightsNotes());
		setAvActivityCommentNotes(bulletin.getAvActivityCommentNotes());
		setSnowpackStructureHighlightsNotes(bulletin.getSnowpackStructureHighlightsNotes());
		setSnowpackStructureCommentNotes(bulletin.getSnowpackStructureCommentNotes());
		setTendencyCommentNotes(bulletin.getTendencyCommentNotes());
		setGeneralHeadlineCommentNotes(bulletin.getGeneralHeadlineCommentNotes());
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
		if (!Objects.equals(this.generalHeadlineCommentTextcat, other.generalHeadlineCommentTextcat))
			return false;
		if (!Objects.equals(this.synopsisCommentTextcat, other.synopsisCommentTextcat))
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
}
