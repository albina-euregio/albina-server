package org.caaml.v6;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.ZonedDateTime;

/**
 * Valid time defines two ISO 8601 timestamps in UTC or with time zone information.
 *
 * Date and Time from and until this bulletin is valid. ISO 8601 Timestamp in UTC or with
 * time zone information.
 */
public class ValidTime {
	@JsonSerialize(using = ToStringSerializer.class)
    private ZonedDateTime endTime;
	@JsonSerialize(using = ToStringSerializer.class)
    private ZonedDateTime startTime;

	public ValidTime() {
	}

	public ValidTime(ZonedDateTime startTime, ZonedDateTime endTime) {
		this.endTime = endTime;
		this.startTime = startTime;
	}

	public ZonedDateTime getEndTime() { return endTime; }
    public void setEndTime(ZonedDateTime value) { this.endTime = value; }

    public ZonedDateTime getStartTime() { return startTime; }
    public void setStartTime(ZonedDateTime value) { this.startTime = value; }
}
