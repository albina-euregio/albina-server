// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Expected avalanche type.
 */
@Serdeable
public enum AvalancheTypeType {
    SLAB, LOOSE, GLIDE;

	@JsonValue
	@Override
	public String toString() {
        switch (this) {
            case SLAB: return "slab";
            case LOOSE: return "loose";
            case GLIDE: return "glide";
        }
        throw new IllegalStateException();
    }

    public static AvalancheTypeType forValue(String value) {
        if (value.equals("slab")) return SLAB;
        if (value.equals("loose")) return LOOSE;
        if (value.equals("glide")) return GLIDE;
        throw new IllegalArgumentException("Cannot deserialize AvalancheTypeType");
    }
}
