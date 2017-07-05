package eu.albina.model;

import java.util.UUID;

import org.joda.time.DateTime;

public class BulletinLock {

	private UUID sessionId;
	private String bulletin;
	private DateTime date;

	public BulletinLock(UUID sessionId, String bulletin, DateTime date) {
		this.sessionId = sessionId;
		this.bulletin = bulletin;
		this.date = date;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public String getBulletin() {
		return bulletin;
	}

	public void setBulletin(String bulletin) {
		this.bulletin = bulletin;
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}
}
