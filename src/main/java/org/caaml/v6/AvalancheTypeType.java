// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Expected avalanche type.
 */
@Serdeable
public enum AvalancheTypeType {
    @JsonProperty("slab") SLAB,
    @JsonProperty("loose") LOOSE,
    @JsonProperty("glide") GLIDE;

	@JsonValue
	@Override
	public String toString() {
        return switch (this) {
            case SLAB -> "slab";
            case LOOSE -> "loose";
            case GLIDE -> "glide";
        };
    }

}
