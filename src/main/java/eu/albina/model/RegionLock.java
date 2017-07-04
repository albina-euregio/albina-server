package eu.albina.model;

import java.util.UUID;

import org.joda.time.DateTime;

public class RegionLock {

	private UUID sessionId;
	private String region;
	private DateTime date;

	public RegionLock(UUID sessionId, String region, DateTime date) {
		this.sessionId = sessionId;
		this.region = region;
		this.date = date;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}
}
