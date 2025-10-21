// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import eu.albina.model.enumerations.AvalancheSize;
import eu.albina.model.enumerations.AvalancheType;
import eu.albina.model.enumerations.Frequency;
import eu.albina.model.enumerations.GlidingSnowActivity;
import eu.albina.model.enumerations.SnowpackStability;
import io.micronaut.serde.annotation.Serdeable;
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
@Serdeable
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
