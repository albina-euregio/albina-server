/*******************************************************************************
 * Copyright (C) 2024 Norbert Lanzanasto
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

import eu.albina.model.enumerations.AvalancheSize;
import eu.albina.model.enumerations.AvalancheType;
import eu.albina.model.enumerations.Frequency;
import eu.albina.model.enumerations.GlidingSnowActivity;
import eu.albina.model.enumerations.SnowpackStability;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * This class holds all catalog of phrases texts for danger source variants.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "danger_source_variant_texts")
public class DangerSourceVariantText extends AbstractPersistentObject {

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_TYPE")
	private AvalancheType avalancheType;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_PROBLEM", length = 191)
	private eu.albina.model.enumerations.AvalancheProblem avalancheProblem;

	@Column(name = "HAS_DAYTIME_DEPENDENCY")
	private Boolean hasDaytimeDependency;

	@Enumerated(EnumType.STRING)
	@Column(name = "GLIDING_SNOW_ACTIVITY")
	private GlidingSnowActivity glidingSnowActivity;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_SIZE", length=191)
	private AvalancheSize avalancheSize;

	@Enumerated(EnumType.STRING)
	@Column(name = "SNOWPACK_STABILITY", length = 191)
	private SnowpackStability snowpackStability;

	@Enumerated(EnumType.STRING)
	@Column(name = "FREQUENCY", length = 191)
	private Frequency frequency;

	@Lob
	@Column(name = "TEXTCAT")
	private String textcat;

	// Getters and Setters
	public AvalancheType getAvalancheType() {
		return avalancheType;
	}

	public void setAvalancheType(AvalancheType avalancheType) {
		this.avalancheType = avalancheType;
	}

	public eu.albina.model.enumerations.AvalancheProblem getAvalancheProblem() {
		return avalancheProblem;
	}

	public void setAvalancheProblem(eu.albina.model.enumerations.AvalancheProblem avalancheProblem) {
		this.avalancheProblem = avalancheProblem;
	}

	public Boolean getHasDaytimeDependency() {
		return hasDaytimeDependency;
	}

	public void setHasDaytimeDependency(Boolean hasDaytimeDependency) {
		this.hasDaytimeDependency = hasDaytimeDependency;
	}

	public GlidingSnowActivity getGlidingSnowActivity() {
		return glidingSnowActivity;
	}

	public void setGlidingSnowActivity(GlidingSnowActivity glidingSnowActivity) {
		this.glidingSnowActivity = glidingSnowActivity;
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

	public String getTextcat() {
		return textcat;
	}

	public void setTextcat(String textcat) {
		this.textcat = textcat;
	}
}
