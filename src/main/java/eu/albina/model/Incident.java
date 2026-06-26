// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.albina.model.converter.JsonConverter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
@Serdeable
@Introspected(excludedAnnotations = {JsonIgnore.class})
public class Incident extends AbstractPersistentObject {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REGION_ID", nullable = false)
	@JsonIgnore
	private Region region;

	@Column(name = "DATE_TIME", nullable = false, insertable = false, updatable = false)
	@Generated(event = {EventType.INSERT, EventType.UPDATE})
	@JsonIgnore
	private String dateTime;

	@Column(name = "CREATED_AT", nullable = false)
	private Instant createdAt;

	@Column(name = "UPDATED_AT", nullable = false)
	private Instant updatedAt;

	@Column(name = "DATA", columnDefinition = "json", nullable = false)
	@Convert(converter = JsonConverter.class)
	private Object data;

	@Column(name = "PUBLISHED_AT")
	private Instant publishedAt;

	@Column(name = "PUBLIC_DATA", columnDefinition = "json")
	@Convert(converter = JsonConverter.class)
	private Object publicData;

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

	public Object getData() { return data; }
	public void setData(Object data) { this.data = data; }

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(Instant publishedAt) {
		this.publishedAt = publishedAt;
	}

	public Object getPublicData() {
		return publicData;
	}

	public void setPublicData(Object publicData) {
		this.publicData = publicData;
	}

	/**
	 * Public projection of an incident, exposing only the fields visible to
	 * unauthenticated clients. Returns {@code null} when the incident has not
	 * been published.
	 */
	@Serdeable
	public record PublicView(String id, Instant publishedAt, Object data) {
	}

	public PublicView getPublicView() {
		return publicData != null ? new PublicView(id, publishedAt,
			// publicData gets exported as data
			publicData) : null;
	}
}
