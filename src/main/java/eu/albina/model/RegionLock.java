// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import io.micronaut.serde.annotation.Serdeable;

import java.time.ZonedDateTime;

@Serdeable
public class RegionLock {

	private String sessionId;
	private String username;
	private String region;
	private ZonedDateTime date;
	private boolean lock;

	public RegionLock() {
	}

	public RegionLock(String username, String region, ZonedDateTime date, boolean lock) {
		this.username = username;
		this.region = region;
		this.date = date;
		this.lock = lock;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(ZonedDateTime date) {
		this.date = date;
	}

	public boolean getLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}
}
