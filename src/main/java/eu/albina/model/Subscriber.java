/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.common.base.Strings;

import eu.albina.model.enumerations.LanguageCode;

@Entity
@Table(name = "subscribers")
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
		regions = new ArrayList<Region>();
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

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		json.put("email", getEmail());
		json.put("confirmed", getConfirmed());
		if (regions != null && regions.size() > 0) {
			JSONArray jsonRegions = new JSONArray();
			for (Region region : regions) {
				jsonRegions.put(region.getId());
			}
			json.put("regions", jsonRegions);
		}
		json.put("language", getLanguage());
		json.put("pdfAttachment", getPdfAttachment());

		return json;
	}

	public boolean affectsRegion(Region region) {
		if (getRegions() != null && region != null && !Strings.isNullOrEmpty(region.getId()))
			return getRegions().stream().anyMatch(entry -> entry.getId().startsWith(region.getId()));
		return false;
	}
}
