// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Valid time period can be used to limit the validity of an element to an earlier or later
 * period. It can be used to distinguish danger ratings or avalanche problems.
 */
@Serdeable
public enum ValidTimePeriod {
    ALL_DAY, EARLIER, LATER;

	@JsonValue
	@Override
	public String toString() {
        return switch (this) {
            case ALL_DAY -> "all_day";
            case EARLIER -> "earlier";
            case LATER -> "later";
        };
    }

}
