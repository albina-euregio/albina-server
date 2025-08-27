// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import eu.albina.model.enumerations.ArtificialAvalancheReleaseProbability;
import eu.albina.model.enumerations.AvalancheSize;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.HazardSiteDistribution;
import eu.albina.model.enumerations.NaturalAvalancheReleaseProbability;

@Embeddable
@Serdeable
public class MatrixInformation {

	@Enumerated(EnumType.STRING)
	@Column(name = "ARTIFICIAL_DANGER_RATING", length = 191)
	private DangerRating artificialDangerRating;

	@Enumerated(EnumType.STRING)
	@Column(name = "ARTIFICIAL_AVALANCHE_SIZE", length = 191)
	private AvalancheSize artificialAvalancheSize;

	@Enumerated(EnumType.STRING)
	@Column(name = "ARTIFICIAL_AVALANCHE_RELEASE_PROBABILITY", length = 191)
	private ArtificialAvalancheReleaseProbability artificialAvalancheReleaseProbability;

	@Enumerated(EnumType.STRING)
	@Column(name = "ARTIFICIAL_HAZARD_SITE_DISTRIBUTION", length = 191)
	private HazardSiteDistribution artificialHazardSiteDistribution;

	@Enumerated(EnumType.STRING)
	@Column(name = "NATURAL_DANGER_RATING", length = 191)
	private DangerRating naturalDangerRating;

	@Enumerated(EnumType.STRING)
	@Column(name = "NATURAL_AVALANCHE_RELEASE_PROBABILITY", length = 191)
	private NaturalAvalancheReleaseProbability naturalAvalancheReleaseProbability;

	@Enumerated(EnumType.STRING)
	@Column(name = "NATURAL_HAZARD_SITE_DISTRIBUTION", length = 191)
	private HazardSiteDistribution naturalHazardSiteDistribution;

	public DangerRating getArtificialDangerRating() {
		return artificialDangerRating;
	}

	public void setArtificialDangerRating(DangerRating artificialDangerRating) {
		this.artificialDangerRating = artificialDangerRating;
	}

	public AvalancheSize getArtificialAvalancheSize() {
		return artificialAvalancheSize;
	}

	public void setArtificialAvalancheSize(AvalancheSize artificialAvalancheSize) {
		this.artificialAvalancheSize = artificialAvalancheSize;
	}

	public ArtificialAvalancheReleaseProbability getArtificialAvalancheReleaseProbability() {
		return artificialAvalancheReleaseProbability;
	}

	public void setArtificialAvalancheReleaseProbability(
			ArtificialAvalancheReleaseProbability artificialAvalancheReleaseProbability) {
		this.artificialAvalancheReleaseProbability = artificialAvalancheReleaseProbability;
	}

	public HazardSiteDistribution getArtificialHazardSiteDistribution() {
		return artificialHazardSiteDistribution;
	}

	public void setArtificialHazardSiteDistribution(HazardSiteDistribution artificialHazardSiteDistribution) {
		this.artificialHazardSiteDistribution = artificialHazardSiteDistribution;
	}

	public DangerRating getNaturalDangerRating() {
		return naturalDangerRating;
	}

	public void setNaturalDangerRating(DangerRating naturalDangerRating) {
		this.naturalDangerRating = naturalDangerRating;
	}

	public NaturalAvalancheReleaseProbability getNaturalAvalancheReleaseProbability() {
		return naturalAvalancheReleaseProbability;
	}

	public void setNaturalAvalancheReleaseProbability(
			NaturalAvalancheReleaseProbability naturalAvalancheReleaseProbability) {
		this.naturalAvalancheReleaseProbability = naturalAvalancheReleaseProbability;
	}

	public HazardSiteDistribution getNaturalHazardSiteDistribution() {
		return naturalHazardSiteDistribution;
	}

	public void setNaturalHazardSiteDistribution(HazardSiteDistribution naturalHazardSiteDistribution) {
		this.naturalHazardSiteDistribution = naturalHazardSiteDistribution;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!MatrixInformation.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final MatrixInformation other = (MatrixInformation) obj;

		if (this.artificialDangerRating != other.artificialDangerRating)
			return false;
		if (this.artificialAvalancheSize != other.artificialAvalancheSize)
			return false;
		if (this.artificialAvalancheReleaseProbability != other.artificialAvalancheReleaseProbability)
			return false;
		if (this.artificialHazardSiteDistribution != other.artificialHazardSiteDistribution)
			return false;
		if (this.naturalDangerRating != other.naturalDangerRating)
			return false;
		if (this.naturalAvalancheReleaseProbability != other.naturalAvalancheReleaseProbability)
			return false;
		if (this.naturalHazardSiteDistribution != other.naturalHazardSiteDistribution)
			return false;

		return true;
	}
}
