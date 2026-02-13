package eu.albina.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
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
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "generic_observations")
public class GenericObservation {

    @EmbeddedId
    private GenericObservationId id;

    @NotNull
    @Column(name = "OBS_TYPE", nullable = false)
    private String obsType;

    @Column(name = "EXTERNAL_URL")
    private String externalUrl;

    @Column(name = "STABILITY")
    private String stability;

    @Column(name = "ASPECTS")
    private String aspects;

    @Size(max = 191)
    @Column(name = "AUTHOR_NAME", length = 191)
    private String authorName;

    @Column(name = "OBS_CONTENT")
    private String obsContent;

    @Column(name = "OBS_DATA")
    private String obsData;

    @Column(name = "ELEVATION")
    private Double elevation;

    @Column(name = "ELEVATION_LOWER_BOUND")
    private Double elevationLowerBound;

    @Column(name = "ELEVATION_UPPER_BOUND")
    private Double elevationUpperBound;

    @Column(name = "EVENT_DATE")
    private Instant eventDate;

    @Column(name = "LATITUDE")
    private Double latitude;

    @Size(max = 191)
    @Column(name = "LOCATION_NAME", length = 191)
    private String locationName;

    @Column(name = "LONGITUDE")
    private Double longitude;

    @Size(max = 191)
    @Column(name = "REGION_ID", length = 191)
    private String regionId;

    @Column(name = "REPORT_DATE")
    private Instant reportDate;

    @Column(name = "AVALANCHE_PROBLEMS")
    private String avalancheProblems;

    @Column(name = "DANGER_PATTERNS")
    private String dangerPatterns;

    @Column(name = "IMPORTANT_OBSERVATION")
    private String importantObservation;

    @Column(name = "EXTRA_DIALOG_ROWS")
    private String extraDialogRows;

    @Column(name = "EXTERNAL_IMG")
    private String externalImg;

    @Column(name = "PERSON_INVOLVEMENT")
    private String personInvolvement;

    @ColumnDefault("0")
    @Column(name = "DELETED")
    private Boolean deleted;

    @ColumnDefault("0")
    @Column(name = "ALLOW_EDIT")
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

    public String getObsType() {
        return obsType;
    }

    public void setObsType(String obsType) {
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

    public String getAspects() {
        return aspects;
    }

    public void setAspects(String aspects) {
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

    public String getObsData() {
        return obsData;
    }

    public void setObsData(String obsData) {
        this.obsData = obsData;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Double getElevationLowerBound() {
        return elevationLowerBound;
    }

    public void setElevationLowerBound(Double elevationLowerBound) {
        this.elevationLowerBound = elevationLowerBound;
    }

    public Double getElevationUpperBound() {
        return elevationUpperBound;
    }

    public void setElevationUpperBound(Double elevationUpperBound) {
        this.elevationUpperBound = elevationUpperBound;
    }

    public Instant getEventDate() {
        return eventDate;
    }

    public void setEventDate(Instant eventDate) {
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

    public Instant getReportDate() {
        return reportDate;
    }

    public void setReportDate(Instant reportDate) {
        this.reportDate = reportDate;
    }

    public String getAvalancheProblems() {
        return avalancheProblems;
    }

    public void setAvalancheProblems(String avalancheProblems) {
        this.avalancheProblems = avalancheProblems;
    }

    public String getDangerPatterns() {
        return dangerPatterns;
    }

    public void setDangerPatterns(String dangerPatterns) {
        this.dangerPatterns = dangerPatterns;
    }

    public String getImportantObservation() {
        return importantObservation;
    }

    public void setImportantObservation(String importantObservation) {
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

    public String getPersonInvolvement() {
        return personInvolvement;
    }

    public void setPersonInvolvement(String personInvolvement) {
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

	@Embeddable
	public static class GenericObservationId implements Serializable {
		@Serial
		private static final long serialVersionUID = -1262080281652170193L;

		@NotNull
		@Column(name = "SOURCE", nullable = false, length = 191)
		private String source;

		@NotNull
		@Column(name = "ID", nullable = false, length = 191)
		private String id;

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
