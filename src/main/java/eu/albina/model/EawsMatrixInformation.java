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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.openjson.JSONObject;

import eu.albina.model.enumerations.AvalancheSize;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.DangerRatingModificator;
import eu.albina.model.enumerations.Frequency;
import eu.albina.model.enumerations.SnowpackStability;

@Embeddable
public class EawsMatrixInformation implements AvalancheInformationObject {

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING")
	private DangerRating dangerRating;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING_MODIFICATOR")
	private DangerRatingModificator dangerRatingModificator;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_SIZE")
	private AvalancheSize avalancheSize;

	@Enumerated(EnumType.STRING)
	@Column(name = "SNOWPACK_STABILITY")
	private SnowpackStability snowpackStability;

	@Enumerated(EnumType.STRING)
	@Column(name = "FREQUENCY")
	private Frequency frequency;

	public EawsMatrixInformation() {
	}

	public EawsMatrixInformation(JSONObject json) {
		this();

		if (json.has("dangerRating"))
			this.dangerRating = DangerRating.fromString(json.getString("dangerRating"));
		if (json.has("dangerRatingModificator"))
			this.dangerRatingModificator = DangerRatingModificator.fromString(json.getString("dangerRatingModificator"));
		if (json.has("avalancheSize"))
			this.avalancheSize = AvalancheSize.fromString(json.getString("avalancheSize"));
		if (json.has("snowpackStability"))
			this.snowpackStability = SnowpackStability.fromString(json.getString("snowpackStability"));
		if (json.has("frequency"))
			this.frequency = Frequency.fromString(json.getString("frequency"));
	}

	public DangerRating getDangerRating() {
		return dangerRating;
	}

	public void setDangerRating(DangerRating dangerRating) {
		this.dangerRating = dangerRating;
	}

	public DangerRatingModificator getDangerRatingModificator() {
		return dangerRatingModificator;
	}

	public void setDangerRatingModificator(DangerRatingModificator dangerRatingModificator) {
		this.dangerRatingModificator = dangerRatingModificator;
	}

	public AvalancheSize getAvalancheSize() {
		return avalancheSize;
	}

	public void setAvalancheSize(AvalancheSize avalancheSize) {
		this.avalancheSize = avalancheSize;
	}

	public SnowpackStability getSnowpackStability() {
		return snowpackStability;
	}

	public void setSnowpackStability(SnowpackStability snowpackStability) {
		this.snowpackStability = snowpackStability;
	}

	public Frequency getFrequency() {
		return frequency;
	}

	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (dangerRating != null)
			json.put("dangerRating", this.dangerRating.toString());
		if (dangerRatingModificator != null)
			json.put("dangerRatingModificator", this.dangerRatingModificator.toString());
		if (avalancheSize != null)
			json.put("avalancheSize", this.avalancheSize.toString());
		if (snowpackStability != null)
			json.put("snowpackStability", this.snowpackStability.toString());
		if (frequency != null)
			json.put("frequency", this.frequency.toString());

		return json;
	}

	public void toCAAMLv6(Document doc, Element rootElement) {
		/*
		// does not exist in CAAMLv6 specification
		if (dangerRating != null) {
			Element dangerRatingElement = doc.createElement("dangerRating");
			dangerRatingElement.appendChild(doc.createTextNode(DangerRating.getCAAMLv6String(dangerRating)));
			rootElement.appendChild(dangerRatingElement);
		}
		*/
		if (avalancheSize != null) {
			Element avalancheSizeElement = doc.createElement("avalancheSize");
			avalancheSizeElement.appendChild(doc.createTextNode(avalancheSize.toCaamlString()));
			rootElement.appendChild(avalancheSizeElement);
		}
		if (snowpackStability != null) {
			Element snowpackStabilityElement = doc.createElement("snowpackStability");
			snowpackStabilityElement
					.appendChild(doc.createTextNode(snowpackStability.toCaamlv6String()));
			rootElement.appendChild(snowpackStabilityElement);
		}
		if (frequency != null) {
			Element frequencyElement = doc.createElement("frequency");
			frequencyElement
					.appendChild(doc.createTextNode(frequency.toCaamlv6String()));
			rootElement.appendChild(frequencyElement);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!EawsMatrixInformation.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final EawsMatrixInformation other = (EawsMatrixInformation) obj;

		if (this.dangerRating != other.dangerRating)
			return false;
		if (this.dangerRatingModificator != other.dangerRatingModificator)
			return false;
		if (this.avalancheSize != other.avalancheSize)
			return false;
		if (this.snowpackStability != other.snowpackStability)
			return false;
		if (this.frequency != other.frequency)
			return false;

		return true;
	}
}
