package eu.albina.model;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import eu.albina.model.converter.EnumSetToStringConverter;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.AvalancheProblem;
import eu.albina.model.enumerations.DangerPattern;

@Entity
@Table(name = "generic_observations")
@Serdeable
public class GenericObservation {
	@EmbeddedId
	@JsonUnwrapped
	private GenericObservationId id;

	@NotNull
	@Column(name = "OBS_TYPE", nullable = false)
	@Enumerated(EnumType.STRING)
	@JsonProperty("$type")
	private ObservationType obsType;

	@Column(name = "EXTERNAL_URL")
	@JsonProperty("$externalUrl")
	private String externalUrl;

	@Column(name = "STABILITY")
	private String stability;

	@Column(name = "ASPECTS", columnDefinition = Aspect.Converter.COLUMN_DEFINITION)
	@Convert(converter = Aspect.Converter.class)
	private Set<Aspect> aspects;

	@Column(name = "AUTHOR_NAME", length = 191)
	private String authorName;

	@Column(name = "OBS_CONTENT")
	@JsonProperty("content")
	private String obsContent;

	@Column(name = "OBS_DATA", columnDefinition = "json")
	@JsonProperty("$data")
	@JsonRawValue
	private Object obsData;

	@Column(name = "ELEVATION")
	private Integer elevation;

	@Column(name = "ELEVATION_LOWER_BOUND")
	private Integer elevationLowerBound;

	@Column(name = "ELEVATION_UPPER_BOUND")
	private Integer elevationUpperBound;

	@Column(name = "EVENT_DATE")
	private ZonedDateTime eventDate;

	@Column(name = "LATITUDE")
	private Double latitude;

	@Column(name = "LOCATION_NAME", length = 191)
	private String locationName;

	@Column(name = "LONGITUDE")
	private Double longitude;

	@Column(name = "REGION_ID", length = 191)
	private String regionId;

	@Column(name = "REPORT_DATE")
	private ZonedDateTime reportDate;

	@Column(name = "AVALANCHE_PROBLEMS", columnDefinition = AvalancheProblem.Converter.COLUMN_DEFINITION)
	@Convert(converter = AvalancheProblem.Converter.class)
	private Set<AvalancheProblem> avalancheProblems;

	@Column(name = "DANGER_PATTERNS", columnDefinition = DangerPattern.Converter.COLUMN_DEFINITION)
	@Convert(converter = DangerPattern.Converter.class)
	private Set<DangerPattern> dangerPatterns;

	@Column(name = "IMPORTANT_OBSERVATION", columnDefinition = ImportantObservation.Converter.COLUMN_DEFINITION)
	@Convert(converter = ImportantObservation.Converter.class)
	private Set<ImportantObservation> importantObservation;

	@Column(name = "EXTRA_DIALOG_ROWS")
	private String extraDialogRows;

	@Column(name = "EXTERNAL_IMG")
	@JsonProperty("$externalImgs")
	private String externalImg;

	@Column(name = "PERSON_INVOLVEMENT")
	@Enumerated(EnumType.STRING)
	private PersonInvolvement personInvolvement;

	@Column(name = "DELETED")
	@JsonProperty("$deleted")
	private Boolean deleted;

	@Column(name = "ALLOW_EDIT")
	@JsonProperty("allowEdit")
	private Boolean allowEdit;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DANGER_SOURCE")
	private DangerSource dangerSource;

	public GenericObservationId getId() {
		return id;
	}

	public void setId(GenericObservationId id) {
		this.id = id;
	}

	public ObservationType getObsType() {
		return obsType;
	}

	public void setObsType(ObservationType obsType) {
		this.obsType = obsType;
	}

	public String getExternalUrl() {
		return externalUrl;
	}

	public void setExternalUrl(String externalUrl) {
		this.externalUrl = externalUrl;
	}

	public String getStability() {
		return stability;
	}

	public void setStability(String stability) {
		this.stability = stability;
	}

	public Set<Aspect> getAspects() {
		return aspects;
	}

	public void setAspects(Set<Aspect> aspects) {
		this.aspects = aspects;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getObsContent() {
		return obsContent;
	}

	public void setObsContent(String obsContent) {
		this.obsContent = obsContent;
	}

	public Object getObsData() {
		return obsData;
	}

	public void setObsData(Object obsData) {
		this.obsData = obsData;
	}

	public Integer getElevation() {
		return elevation;
	}

	public void setElevation(Integer elevation) {
		this.elevation = elevation;
	}

	public Integer getElevationLowerBound() {
		return elevationLowerBound;
	}

	public void setElevationLowerBound(Integer elevationLowerBound) {
		this.elevationLowerBound = elevationLowerBound;
	}

	public Integer getElevationUpperBound() {
		return elevationUpperBound;
	}

	public void setElevationUpperBound(Integer elevationUpperBound) {
		this.elevationUpperBound = elevationUpperBound;
	}

	public ZonedDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(ZonedDateTime eventDate) {
		this.eventDate = eventDate;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public ZonedDateTime getReportDate() {
		return reportDate;
	}

	public void setReportDate(ZonedDateTime reportDate) {
		this.reportDate = reportDate;
	}

	public Set<AvalancheProblem> getAvalancheProblems() {
		return avalancheProblems;
	}

	public void setAvalancheProblems(Set<AvalancheProblem> avalancheProblems) {
		this.avalancheProblems = avalancheProblems;
	}

	public Set<DangerPattern> getDangerPatterns() {
		return dangerPatterns;
	}

	public void setDangerPatterns(Set<DangerPattern> dangerPatterns) {
		this.dangerPatterns = dangerPatterns;
	}

	public Set<ImportantObservation> getImportantObservation() {
		return importantObservation;
	}

	public void setImportantObservation(Set<ImportantObservation> importantObservation) {
		this.importantObservation = importantObservation;
	}

	public String getExtraDialogRows() {
		return extraDialogRows;
	}

	public void setExtraDialogRows(String extraDialogRows) {
		this.extraDialogRows = extraDialogRows;
	}

	public String getExternalImg() {
		return externalImg;
	}

	public void setExternalImg(String externalImg) {
		this.externalImg = externalImg;
	}

	public PersonInvolvement getPersonInvolvement() {
		return personInvolvement;
	}

	public void setPersonInvolvement(PersonInvolvement personInvolvement) {
		this.personInvolvement = personInvolvement;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Boolean getAllowEdit() {
		return allowEdit;
	}

	public void setAllowEdit(Boolean allowEdit) {
		this.allowEdit = allowEdit;
	}

	public DangerSource getDangerSource() {
		return dangerSource;
	}

	public void setDangerSource(DangerSource dangerSource) {
		this.dangerSource = dangerSource;
	}

	public enum ObservationType {
		SimpleObservation, Evaluation, Avalanche, Blasting, Closure, Profile, TimeSeries, Webcam, DrySnowfallLevel
	}

	public enum PersonInvolvement {
		Dead, Injured, Uninjured, No, Unknown
	}

	public enum ImportantObservation {
		SnowLine, SurfaceHoar, Graupel, StabilityTest, IceFormation, VeryLightNewSnow, ForBlog;

		@jakarta.persistence.Converter
		public static class Converter extends EnumSetToStringConverter<ImportantObservation> {

			public static final String COLUMN_DEFINITION = "set('SnowLine',.'SurfaceHoar',.'Graupel',.'StabilityTest',.'IceFormation',.'VeryLightNewSnow',.'ForBlog')";

			@Override
			protected Class<ImportantObservation> getEnumClass() {
				return ImportantObservation.class;
			}
		}
	}

	@Embeddable
	@Serdeable
	public static class GenericObservationId {
		@NotNull
		@Column(name = "SOURCE", nullable = false, length = 191)
		@JsonProperty("$source")
		private String source;

		@NotNull
		@Column(name = "ID", nullable = false, length = 191)
		@JsonProperty("$id")
		private String id;

		public GenericObservationId() {
		}

		public GenericObservationId(String source, String id) {
			this.source = source;
			this.id = id;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			GenericObservationId entity = (GenericObservationId) o;
			return Objects.equals(this.source, entity.source) &&
				Objects.equals(this.id, entity.id);
		}

		@Override
		public int hashCode() {
			return Objects.hash(source, id);
		}
	}
}
