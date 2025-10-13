// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Valid time defines two ISO 8601 timestamps in UTC or with time zone information.
 *
 * Date and Time from and until this bulletin is valid. ISO 8601 Timestamp in UTC or with
 * time zone information.
 */
@JsonPropertyOrder({"startTime", "endTime"})
@Serdeable
public class ValidTime {
	@JsonSerialize(as = String.class)
    private Instant endTime;
	@JsonSerialize(as = String.class)
    private Instant startTime;

	public ValidTime() {
	}

	public ValidTime(Instant startTime, Instant endTime) {
		this.endTime = endTime;
		this.startTime = startTime;
	}

	public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant value) { this.endTime = value; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant value) { this.startTime = value; }
}
