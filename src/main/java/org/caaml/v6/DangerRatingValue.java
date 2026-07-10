// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Danger rating value, according to EAWS danger scale definition.
 */
@Serdeable
public enum DangerRatingValue {
    considerable, high, low, moderate, no_rating, no_snow, very_high;

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
