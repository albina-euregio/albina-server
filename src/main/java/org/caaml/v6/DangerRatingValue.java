// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.util.Comparator;
import java.util.List;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Danger rating value, according to EAWS danger scale definition.
 */
@Serdeable
public enum DangerRatingValue {
    considerable, high, low, moderate, no_rating, no_snow, very_high;

	/** The values of the EAWS danger scale, ordered from lowest to highest. The constants above are ordered alphabetically. */
	private static final List<DangerRatingValue> SEVERITY = List.of(no_snow, no_rating, low, moderate, considerable, high, very_high);

	public static final Comparator<DangerRatingValue> BY_SEVERITY = Comparator.comparingInt(SEVERITY::indexOf);

	/** The level of the EAWS danger scale, or 0 if there is no rating. */
	public int level() {
		return switch (this) {
			case no_snow, no_rating -> 0;
			case low -> 1;
			case moderate -> 2;
			case considerable -> 3;
			case high -> 4;
			case very_high -> 5;
		};
	}

	/** The level of the EAWS danger scale, or 0 if there is no rating at all. */
	public static int level(DangerRatingValue dangerRatingValue) {
		return dangerRatingValue == null ? 0 : dangerRatingValue.level();
	}

	/** The colour of the EAWS danger scale. */
	public String color() {
		return switch (this) {
			case low -> "#CCFF66";
			case moderate -> "#FFFF00";
			case considerable -> "#FF9900";
			case high -> "#FF0000";
			case very_high -> "#800000";
			case no_snow, no_rating -> "#969696";
		};
	}

    public static DangerRatingValue forValue(String value) {
		return switch (value) {
			case "considerable" -> considerable;
			case "high" -> high;
			case "low" -> low;
			case "moderate" -> moderate;
			case "no_rating" -> no_rating;
			case "no_snow" -> no_snow;
			case "very_high" -> very_high;
			default -> throw new IllegalArgumentException("Cannot deserialize DangerRatingValue");
		};
	}
}
