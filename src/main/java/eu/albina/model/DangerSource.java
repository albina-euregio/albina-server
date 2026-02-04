// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.Instant;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * This class holds all information about one danger source.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "danger_sources")
@Serdeable
public class DangerSource extends AbstractPersistentObject {

	@Column(name = "OWNER_REGION", length = 191)
	private String ownerRegion;

	@Column(name = "CREATION_DATE")
	private Instant creationDate;

	@Column(name = "TITLE")
	private String title;

	@Column(name = "DESCRIPTION", columnDefinition = "longtext")
	private String description;

	public String getOwnerRegion() {
		return ownerRegion;
	}

	public void setOwnerRegion(String ownerRegion) {
		this.ownerRegion = ownerRegion;
	}

	public Instant getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
