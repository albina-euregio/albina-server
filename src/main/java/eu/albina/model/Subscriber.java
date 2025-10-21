// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.util.ArrayList;
import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import com.google.common.base.Strings;

import eu.albina.model.enumerations.LanguageCode;

@Entity
@Table(name = "subscribers")
@Serdeable
public class Subscriber {

	/** Email address of the subscriber */
	@Id
	@Column(name = "EMAIL", length = 191)
	private String email;

	@Column(name = "CONFIRMED")
	private boolean confirmed;

	@ManyToMany
	@JoinTable(name="subscriber_regions",
	 joinColumns=@JoinColumn(name="SUBSCRIBER_ID"),
	 inverseJoinColumns=@JoinColumn(name="REGION_ID")
	)
	private List<Region> regions;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE", length = 191)
	private LanguageCode language;

	@Column(name = "PDF_ATTACHMENT")
	private boolean pdfAttachment;

	/**
	 * Standard constructor for a subscriber.
	 */
	public Subscriber() {
		regions = new ArrayList<>();
		pdfAttachment = false;
		confirmed = false;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean getConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public List<Region> getRegions() {
		return regions;
	}

	public void setRegions(List<Region> regions) {
		this.regions = regions;
	}

	public void addRole(Region region) {
		if (!this.regions.contains(region))
			this.regions.add(region);
	}

	public LanguageCode getLanguage() {
		return language;
	}

	public void setLanguage(LanguageCode language) {
		this.language = language;
	}

	public boolean getPdfAttachment() {
		return pdfAttachment;
	}

	public void setPdfAttachment(boolean pdfAttachment) {
		this.pdfAttachment = pdfAttachment;
	}

	public boolean affectsRegion(Region region) {
		if (getRegions() != null && region != null && !Strings.isNullOrEmpty(region.getId()))
			return getRegions().stream().anyMatch(entry -> entry.getId().startsWith(region.getId()));
		return false;
	}
}
