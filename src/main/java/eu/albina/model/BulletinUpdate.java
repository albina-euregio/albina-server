// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.Instant;

import eu.albina.model.enumerations.BulletinStatus;

public class BulletinUpdate {

	private String region;
	private Instant date;
	private BulletinStatus status;

	public BulletinUpdate() {
	}

	public BulletinUpdate(String region, Instant date, BulletinStatus status) {
		this.region = region;
		this.date = date;
		this.status = status;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public Instant getDate() {
		return date;
	}

	public void setDate(Instant date) {
		this.date = date;
	}

	public BulletinStatus getStatus() {
		return status;
	}

	public void setStatus(BulletinStatus status) {
		this.status = status;
	}
}
