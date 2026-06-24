// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "incidents")
public class Incident extends AbstractPersistentObject {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REGION_ID", nullable = false)
	private Region region;

	@Column(name = "DATE_TIME", nullable = false)
	private String dateTime;

	@Column(name = "CREATED_AT", nullable = false)
	private Instant createdAt;

	@Column(name = "UPDATED_AT", nullable = false)
	private Instant updatedAt;

	@Column(name = "DATA", columnDefinition = "json", nullable = false)
	private String data;

	@Column(name = "PUBLISHED_AT")
	private Instant publishedAt;

	@Column(name = "PUBLIC_DATA", columnDefinition = "json")
	private String publicData;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	public Region getRegion() { return region; }
	public void setRegion(Region region) { this.region = region; }

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	public String getData() { return data; }
	public void setData(String data) { this.data = data; }

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(Instant publishedAt) {
		this.publishedAt = publishedAt;
	}

	public String getPublicData() {
		return publicData;
	}

	public void setPublicData(String publicData) {
		this.publicData = publicData;
	}
}
