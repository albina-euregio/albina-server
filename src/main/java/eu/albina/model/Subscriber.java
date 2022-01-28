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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

import eu.albina.model.enumerations.LanguageCode;

@Entity
@Table(name = "subscriber")
public class Subscriber {

	/** Email address of the subscriber */
	@Id
	@Column(name = "EMAIL")
	private String email;

	@Column(name = "CONFIRMED")
	private boolean confirmed;

	// TODO use region table
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "subscriber_regions", joinColumns = @JoinColumn(name = "SUBSCRIBER_ID"))
	@Column(name = "REGION_ID")
	private List<String> regions;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE")
	private LanguageCode language;

	@Column(name = "PDF_ATTACHMENT")
	private boolean pdfAttachment;

	/**
	 * Standard constructor for a subscriber.
	 */
	public Subscriber() {
		regions = new ArrayList<String>();
		pdfAttachment = false;
		confirmed = false;
	}

	public Subscriber(JSONObject json) {
		this();
		if (json.has("email") && !json.isNull("email"))
			this.email = json.getString("email");
		if (json.has("confirmed") && !json.isNull("confirmed"))
			this.confirmed = json.getBoolean("confirmed");
		if (json.has("regions")) {
			JSONArray regions = json.getJSONArray("regions");
			for (Object entry : regions) {
				this.regions.add((String) entry);
			}
		}
		if (json.has("language") && !json.isNull("language"))
			this.language = LanguageCode.valueOf((json.getString("language").toLowerCase()));
		if (json.has("pdfAttachment") && !json.isNull("pdfAttachment"))
			this.pdfAttachment = json.getBoolean("pdfAttachment");
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

	public List<String> getRegions() {
		return regions;
	}

	public void setRegions(List<String> regions) {
		this.regions = regions;
	}

	public void addRole(String region) {
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
			for (String region : regions) {
				jsonRegions.put(region);
			}
			json.put("regions", jsonRegions);
		}
		json.put("language", getLanguage());
		json.put("pdfAttachment", getPdfAttachment());

		return json;
	}

	public boolean affectsRegion(String region) {
		if (getRegions() != null)
			return getRegions().stream().anyMatch(entry -> entry.startsWith(region));
		return false;
	}
}
