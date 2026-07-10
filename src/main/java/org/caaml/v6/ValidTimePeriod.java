// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Valid time period can be used to limit the validity of an element to an earlier or later
 * period. It can be used to distinguish danger ratings or avalanche problems.
 */
@Serdeable
public enum ValidTimePeriod {
    @JsonProperty("all_day") ALL_DAY,
    @JsonProperty("earlier") EARLIER,
    @JsonProperty("later") LATER;

}
