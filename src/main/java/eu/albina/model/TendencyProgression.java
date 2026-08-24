// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import eu.albina.model.enumerations.DangerRating;

@Serdeable
public record TendencyProgression(
	@Schema(description = "Start dates of the bulletins of the preceding days") List<Instant> dates,
	@Schema(description = "Highest danger rating of each of these days, per micro region") Map<String, List<DangerRating>> dangerRatings) {
}
