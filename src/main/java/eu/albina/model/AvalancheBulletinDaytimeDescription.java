// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import eu.albina.model.enumerations.Complexity;
import eu.albina.model.enumerations.DangerRating;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "avalanche_bulletin_daytime_descriptions")
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
@Introspected(excludedAnnotations = {JsonIgnore.class})
public class AvalancheBulletinDaytimeDescription extends AbstractPersistentObject {

	@OneToOne
	@PrimaryKeyJoinColumn
	private AvalancheBulletin avalancheBulletin;

	@Column(name = "HAS_ELEVATION_DEPENDENCY")
	private boolean hasElevationDependency;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING_ABOVE", length = 191)
	private DangerRating dangerRatingAbove;

	@Column(name = "ELEVATION")
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int elevation;

	@Column(name = "TREELINE")
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private boolean treeline;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING_BELOW", length = 191)
	private DangerRating dangerRatingBelow;

	@Enumerated(EnumType.STRING)
	@Column(name = "COMPLEXITY", length = 191)
	private Complexity complexity;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_PROBLEM_1_ID")
	private AvalancheProblem avalancheProblem1;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_PROBLEM_2_ID")
	private AvalancheProblem avalancheProblem2;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_PROBLEM_3_ID")
	private AvalancheProblem avalancheProblem3;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_PROBLEM_4_ID")
	private AvalancheProblem avalancheProblem4;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_PROBLEM_5_ID")
	private AvalancheProblem avalancheProblem5;

	public String getWarningLevelId() {
		if (isHasElevationDependency())
			return DangerRating.getString(dangerRating(false)) + "_"
				+ DangerRating.getString(dangerRating(true));
		else
			return DangerRating.getString(dangerRating(true)) + "_"
				+ DangerRating.getString(dangerRating(true));
	}

	public boolean isHasElevationDependency() {
		if (isDangerLevelElevationDependency()) {
			return hasElevationDependency;
		} else {
			return false;
		}
	}

	public void setHasElevationDependency(boolean hasElevationDependency) {
		this.hasElevationDependency = hasElevationDependency;
	}

	public int getElevation() {
		if (isDangerLevelElevationDependency()) {
			return elevation;
		} else {
			return 0;
		}
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public boolean getTreeline() {
		if (isDangerLevelElevationDependency()) {
			return treeline;
		} else {
			return false;
		}
	}

	public void setTreeline(boolean treeline) {
		this.treeline = treeline;
	}

	public DangerRating getDangerRatingAbove() {
		return dangerRatingAbove;
	}

	public void setDangerRatingAbove(DangerRating dangerRatingAbove) {
		this.dangerRatingAbove = dangerRatingAbove;
	}

	public DangerRating getDangerRatingBelow() {
		return dangerRatingBelow;
	}

	public void setDangerRatingBelow(DangerRating dangerRatingBelow) {
		this.dangerRatingBelow = dangerRatingBelow;
	}

	public Complexity getComplexity() {
		return complexity;
	}

	public void setComplexity(Complexity complexity) {
		this.complexity = complexity;
	}

	public AvalancheProblem getAvalancheProblem1() {
		return avalancheProblem1;
	}

	public void setAvalancheProblem1(AvalancheProblem avalancheProblem1) {
		this.avalancheProblem1 = avalancheProblem1;
	}

	public AvalancheProblem getAvalancheProblem2() {
		return avalancheProblem2;
	}

	public void setAvalancheProblem2(AvalancheProblem avalancheProblem2) {
		this.avalancheProblem2 = avalancheProblem2;
	}

	public AvalancheProblem getAvalancheProblem3() {
		return avalancheProblem3;
	}

	public void setAvalancheProblem3(AvalancheProblem avalancheProblem3) {
		this.avalancheProblem3 = avalancheProblem3;
	}

	public AvalancheProblem getAvalancheProblem4() {
		return avalancheProblem4;
	}

	public void setAvalancheProblem4(AvalancheProblem avalancheProblem4) {
		this.avalancheProblem4 = avalancheProblem4;
	}

	public AvalancheProblem getAvalancheProblem5() {
		return avalancheProblem5;
	}

	public void setAvalancheProblem5(AvalancheProblem avalancheProblem5) {
		this.avalancheProblem5 = avalancheProblem5;
	}

	@JsonIgnore
	public List<AvalancheProblem> getAvalancheProblems() {
		return Arrays.asList(avalancheProblem1, avalancheProblem2, avalancheProblem3, avalancheProblem4,
				avalancheProblem5);
	}

	public DangerRating dangerRating(boolean above) {
		if (isDangerLevelElevationDependency()) {
			if (isHasElevationDependency() && !above) {
				return getDangerRatingBelow();
			} else {
				return getDangerRatingAbove();
			}
		} else {
			return highestDangerRating();
		}
	}

	private DangerRating highestDangerRating() {
		DangerRating highest = null;
		if (getDangerRatingAbove() != null) {
			highest = getDangerRatingAbove();
			if (getDangerRatingBelow() != null && getDangerRatingBelow().ordinal() > highest.ordinal()) {
				highest = getDangerRatingBelow();
			}
		} else if (getDangerRatingBelow() != null) {
			highest = getDangerRatingBelow();
		}
		return highest;
	}

	@JsonIgnore
	boolean isDangerLevelElevationDependency() {
		return true; // FIXME
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!AvalancheBulletinDaytimeDescription.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final AvalancheBulletinDaytimeDescription other = (AvalancheBulletinDaytimeDescription) obj;

		if (this.hasElevationDependency != other.hasElevationDependency)
			return false;
		if (this.elevation != other.elevation)
			return false;
		if (this.treeline != other.treeline)
			return false;
		if (!Objects.equals(this.dangerRatingAbove, other.dangerRatingAbove))
			return false;
		if (!Objects.equals(this.dangerRatingBelow, other.dangerRatingBelow))
			return false;
		if (!Objects.equals(this.complexity, other.complexity))
			return false;
		if (!Objects.equals(this.avalancheProblem1, other.avalancheProblem1))
			return false;
		if (!Objects.equals(this.avalancheProblem2, other.avalancheProblem2))
			return false;
		if (!Objects.equals(this.avalancheProblem3, other.avalancheProblem3))
			return false;
		if (!Objects.equals(this.avalancheProblem4, other.avalancheProblem4))
			return false;
		if (!Objects.equals(this.avalancheProblem5, other.avalancheProblem5))
			return false;

		return true;
	}
}
