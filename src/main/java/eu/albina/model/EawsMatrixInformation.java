// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.albina.model.enumerations.AvalancheSize;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.DangerRatingModificator;
import eu.albina.model.enumerations.Frequency;
import eu.albina.model.enumerations.SnowpackStability;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Comparator;

@Embeddable
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
@Introspected(excludedAnnotations = {JsonIgnore.class})
public class EawsMatrixInformation implements Comparable<EawsMatrixInformation> {

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING", length = 191)
	private DangerRating dangerRating;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING_MODIFICATOR", length = 191)
	private DangerRatingModificator dangerRatingModificator;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_SIZE", length = 191)
	private AvalancheSize avalancheSize;

	@Enumerated(EnumType.STRING)
	@Column(name = "SNOWPACK_STABILITY", length = 191)
	private SnowpackStability snowpackStability;

	@Enumerated(EnumType.STRING)
	@Column(name = "FREQUENCY", length = 191)
	private Frequency frequency;

	@Column(name = "AVALANCHE_SIZE_VALUE")
	private int avalancheSizeValue;

	@Column(name = "SNOWPACK_STABILITY_VALUE")
	private int snowpackStabilityValue;

	@Column(name = "FREQUENCY_VALUE")
	private int frequencyValue;

	public EawsMatrixInformation() {
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

	public int getAvalancheSizeValue() {
		return avalancheSizeValue;
	}

	public void setAvalancheSizeValue(int avalancheSizeValue) {
		this.avalancheSizeValue = avalancheSizeValue;
	}

	public int getSnowpackStabilityValue() {
		return snowpackStabilityValue;
	}

	public void setSnowpackStabilityValue(int snowpackStabilityValue) {
		this.snowpackStabilityValue = snowpackStabilityValue;
	}

	public int getFrequencyValue() {
		return frequencyValue;
	}

	public void setFrequencyValue(int frequencyValue) {
		this.frequencyValue = frequencyValue;
	}

	@JsonIgnore
	public DangerRating getPrimaryDangerRatingFromParameters() {
		switch (getSnowpackStability()) {
			case fair:
				switch (getFrequency()) {
					case none:
						return DangerRating.low;
					case few:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.low;
							case medium:
								return DangerRating.low;
							case large:
								return DangerRating.moderate;
							case very_large:
								return DangerRating.moderate;
							case extreme:
								return DangerRating.considerable;
							default:
								return DangerRating.missing;
						}
					case some:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.low;
							case medium:
								return DangerRating.moderate;
							case large:
								return DangerRating.moderate;
							case very_large:
								return DangerRating.considerable;
							case extreme:
								return DangerRating.considerable;
							default:
								return DangerRating.missing;
						}
					case many:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.low;
							case medium:
								return DangerRating.moderate;
							case large:
								return DangerRating.considerable;
							case very_large:
								return DangerRating.considerable;
							case extreme:
								return DangerRating.high;
							default:
								return DangerRating.missing;
						}
					default:
						return DangerRating.missing;
				}
			case poor:
				switch (getFrequency()) {
					case none:
						return DangerRating.low;
					case few:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.low;
							case medium:
								return DangerRating.moderate;
							case large:
								return DangerRating.moderate;
							case very_large:
								return DangerRating.considerable;
							case extreme:
								return DangerRating.considerable;
							default:
								return DangerRating.missing;
						}
					case some:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.moderate;
							case medium:
								return DangerRating.moderate;
							case large:
								return DangerRating.considerable;
							case very_large:
								return DangerRating.high;
							case extreme:
								return DangerRating.high;
							default:
								return DangerRating.missing;
						}
					case many:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.moderate;
							case medium:
								return DangerRating.considerable;
							case large:
								return DangerRating.high;
							case very_large:
								return DangerRating.high;
							case extreme:
								return DangerRating.very_high;
							default:
								return DangerRating.missing;
						}
					default:
						return DangerRating.missing;
				}
			case very_poor:
				switch (getFrequency()) {
					case none:
						return DangerRating.low;
					case few:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.low;
							case medium:
								return DangerRating.moderate;
							case large:
								return DangerRating.considerable;
							case very_large:
								return DangerRating.considerable;
							case extreme:
								return DangerRating.high;
							default:
								return DangerRating.missing;
						}
					case some:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.moderate;
							case medium:
								return DangerRating.considerable;
							case large:
								return DangerRating.considerable;
							case very_large:
								return DangerRating.high;
							case extreme:
								return DangerRating.very_high;
							default:
								return DangerRating.missing;
						}
					case many:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.moderate;
							case medium:
								return DangerRating.considerable;
							case large:
								return DangerRating.high;
							case very_large:
								return DangerRating.very_high;
							case extreme:
								return DangerRating.very_high;
							default:
								return DangerRating.missing;
						}
					default:
						return DangerRating.missing;
				}
			default:
				return DangerRating.missing;
		}
	}

	@JsonIgnore
	public DangerRating getSecondaryDangerRatingFromParameters() {
		switch (getSnowpackStability()) {
			case fair:
				switch (getFrequency()) {
					case none:
						return DangerRating.low;
					case few:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.missing;
							case medium:
								return DangerRating.moderate;
							case large:
								return DangerRating.low;
							case very_large:
								return DangerRating.considerable;
							case extreme:
								return DangerRating.missing;
							default:
								return DangerRating.missing;
						}
					case some:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.moderate;
							case medium:
								return DangerRating.missing;
							case large:
								return DangerRating.considerable;
							case very_large:
								return DangerRating.missing;
							case extreme:
								return DangerRating.high;
							default:
								return DangerRating.missing;
						}
					case many:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.moderate;
							case medium:
								return DangerRating.missing;
							case large:
								return DangerRating.moderate;
							case very_large:
								return DangerRating.high;
							case extreme:
								return DangerRating.considerable;
							default:
								return DangerRating.missing;
						}
					default:
						return DangerRating.missing;
				}
			case poor:
				switch (getFrequency()) {
					case none:
						return DangerRating.low;
					case few:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.missing;
							case medium:
								return DangerRating.low;
							case large:
								return DangerRating.considerable;
							case very_large:
								return DangerRating.missing;
							case extreme:
								return DangerRating.high;
							default:
								return DangerRating.missing;
						}
					case some:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.low;
							case medium:
								return DangerRating.considerable;
							case large:
								return DangerRating.missing;
							case very_large:
								return DangerRating.considerable;
							case extreme:
								return DangerRating.missing;
							default:
								return DangerRating.missing;
						}
					case many:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.missing;
							case medium:
								return DangerRating.missing;
							case large:
								return DangerRating.considerable;
							case very_large:
								return DangerRating.missing;
							case extreme:
								return DangerRating.high;
							default:
								return DangerRating.missing;
						}
					default:
						return DangerRating.missing;
				}
			case very_poor:
				switch (getFrequency()) {
					case none:
						return DangerRating.low;
					case few:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.moderate;
							case medium:
								return DangerRating.missing;
							case large:
								return DangerRating.moderate;
							case very_large:
								return DangerRating.high;
							case extreme:
								return DangerRating.missing;
							default:
								return DangerRating.missing;
						}
					case some:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.missing;
							case medium:
								return DangerRating.moderate;
							case large:
								return DangerRating.high;
							case very_large:
								return DangerRating.missing;
							case extreme:
								return DangerRating.high;
							default:
								return DangerRating.missing;
						}
					case many:
						switch (getAvalancheSize()) {
							case small:
								return DangerRating.considerable;
							case medium:
								return DangerRating.high;
							case large:
								return DangerRating.missing;
							case very_large:
								return DangerRating.high;
							case extreme:
								return DangerRating.missing;
							default:
								return DangerRating.missing;
						}
					default:
						return DangerRating.missing;
				}
			default:
				return DangerRating.missing;
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
		if (this.avalancheSizeValue != other.avalancheSizeValue)
			return false;
		if (this.snowpackStabilityValue != other.snowpackStabilityValue)
			return false;
		if (this.frequencyValue != other.frequencyValue)
			return false;

		return true;
	}

	private static final Comparator<EawsMatrixInformation> COMPARATOR = Comparator
		.comparing(EawsMatrixInformation::getDangerRating)
		.thenComparing(EawsMatrixInformation::getPrimaryDangerRatingFromParameters)
		.thenComparing(EawsMatrixInformation::getSecondaryDangerRatingFromParameters)
		.thenComparing(EawsMatrixInformation::getSnowpackStability)
		.thenComparing(EawsMatrixInformation::getAvalancheSize);

	@Override
	public int compareTo(EawsMatrixInformation other) {
		return COMPARATOR.compare(this, other);
	}
}
