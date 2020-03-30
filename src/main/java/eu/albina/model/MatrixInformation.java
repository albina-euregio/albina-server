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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.github.openjson.JSONObject;

import eu.albina.model.enumerations.ArtificialAvalancheReleaseProbability;
import eu.albina.model.enumerations.AvalancheSize;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.HazardSiteDistribution;
import eu.albina.model.enumerations.NaturalAvalancheReleaseProbability;

@Embeddable
public class MatrixInformation implements AvalancheInformationObject {

	@Enumerated(EnumType.STRING)
	@Column(name = "ARTIFICIAL_DANGER_RATING")
	private DangerRating artificialDangerRating;

	@Enumerated(EnumType.STRING)
	@Column(name = "ARTIFICIAL_AVALANCHE_SIZE")
	private AvalancheSize artificialAvalancheSize;

	@Enumerated(EnumType.STRING)
	@Column(name = "ARTIFICIAL_AVALANCHE_RELEASE_PROBABILITY")
	private ArtificialAvalancheReleaseProbability artificialAvalancheReleaseProbability;

	@Enumerated(EnumType.STRING)
	@Column(name = "ARTIFICIAL_HARZARD_SITE_DISTRIBUTION")
	private HazardSiteDistribution artificialHazardSiteDistribution;

	@Enumerated(EnumType.STRING)
	@Column(name = "NATURAL_DANGER_RATING")
	private DangerRating naturalDangerRating;

	@Enumerated(EnumType.STRING)
	@Column(name = "NATURAL_AVALANCHE_RELEASE_PROBABILITY")
	private NaturalAvalancheReleaseProbability naturalAvalancheReleaseProbability;

	@Enumerated(EnumType.STRING)
	@Column(name = "NATURAL_HAZARD_SITE_DISTRIBUTION")
	private HazardSiteDistribution naturalHazardSiteDistribution;

	public MatrixInformation() {
	}

	public MatrixInformation(JSONObject json) {
		this();

		if (json.has("artificialDangerRating"))
			this.artificialDangerRating = DangerRating.fromString(json.getString("artificialDangerRating"));
		if (json.has("artificialAvalancheSize"))
			this.artificialAvalancheSize = AvalancheSize.fromString(json.getString("artificialAvalancheSize"));
		if (json.has("artificialAvalancheReleaseProbability"))
			this.artificialAvalancheReleaseProbability = ArtificialAvalancheReleaseProbability
					.fromString(json.getString("artificialAvalancheReleaseProbability"));
		if (json.has("artificialHazardSiteDistribution"))
			this.artificialHazardSiteDistribution = HazardSiteDistribution
					.fromString(json.getString("artificialHazardSiteDistribution"));
		if (json.has("naturalDangerRating"))
			this.naturalDangerRating = DangerRating.fromString(json.getString("naturalDangerRating"));
		if (json.has("naturalAvalancheReleaseProbability"))
			this.naturalAvalancheReleaseProbability = NaturalAvalancheReleaseProbability
					.fromString(json.getString("naturalAvalancheReleaseProbability"));
		if (json.has("naturalHazardSiteDistribution"))
			this.naturalHazardSiteDistribution = HazardSiteDistribution
					.fromString(json.getString("naturalHazardSiteDistribution"));
	}

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
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (artificialDangerRating != null)
			json.put("artificialDangerRating", this.artificialDangerRating.toString());
		if (artificialAvalancheSize != null)
			json.put("artificialAvalancheSize", this.artificialAvalancheSize.toString());
		if (artificialAvalancheReleaseProbability != null)
			json.put("artificialAvalancheReleaseProbability", this.artificialAvalancheReleaseProbability.toString());
		if (artificialHazardSiteDistribution != null)
			json.put("artificialHazardSiteDistribution", this.artificialHazardSiteDistribution.toString());
		if (naturalDangerRating != null)
			json.put("naturalDangerRating", this.naturalDangerRating.toString());
		if (naturalAvalancheReleaseProbability != null)
			json.put("naturalAvalancheReleaseProbability", this.naturalAvalancheReleaseProbability.toString());
		if (naturalHazardSiteDistribution != null)
			json.put("naturalHazardSiteDistribution", this.naturalHazardSiteDistribution.toString());

		return json;
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
