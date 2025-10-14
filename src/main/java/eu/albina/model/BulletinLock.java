// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;

@Serdeable
public class BulletinLock {

	private String sessionId;
	private boolean lock;
	private String bulletin;
	private String userEmail;
	private String userName;
	private Instant date;

	public BulletinLock() {
	}

	public BulletinLock(String sessionId, String bulletin, Instant date, boolean lock, String userEmail, String userName) {
		this.sessionId = sessionId;
		this.bulletin = bulletin;
		this.date = date;
		this.userEmail = userEmail;
		this.userName = userName;
		this.lock = lock;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getBulletin() {
		return bulletin;
	}

	public void setBulletin(String bulletin) {
		this.bulletin = bulletin;
	}

	public Instant getDate() {
		return date;
	}

	public void setDate(Instant date) {
		this.date = date;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean getLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}
}
