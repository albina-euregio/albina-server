/*******************************************************************************
 * Copyright (C) 2024 Norbert Lanzanasto
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

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.LoggerFactory;

import com.github.openjson.JSONObject;
import com.google.common.base.Strings;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * This class holds all information about one danger source.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "danger_sources")
public class DangerSource extends AbstractPersistentObject
		implements AvalancheInformationObject {

	/** Information about the author of the danger source */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	private User user;

	@Column(name = "OWNER_REGION")		
	private String ownerRegion;
		
	@Column(name = "CREATION_DATE")
	private ZonedDateTime creationDate;
	
	@Column(name = "DESCRIPTION")
	private String description;
		
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "danger_source_danger_source_variants", joinColumns = @JoinColumn(name = "DANGER_SOURCE_VARIANT_ID"))
	@Column(name = "DANGER_SOURCE_VARIANTS")
	private Set<DangerSourceVariant> dangerSourceVariants;


	/**
	 * Standard constructor for a danger source.
	 */
	public DangerSource() {
	}

	/**
	 * Custom constructor that creates a danger source object from JSON input.
	 *
	 * @param json
	 *            JSONObject holding information about a danger source.
	 */
	public DangerSource(JSONObject json, Function<String, User> userFunction) {
		this();

		if (json.has("id")) {
			this.id = json.getString("id");
			
			if (json.has("author")) {
				JSONObject author = json.getJSONObject("author");
				if (author.has("email")) {
					try {
						this.user = userFunction.apply(author.getString("email"));
					} catch (Exception e) {
						LoggerFactory.getLogger(getClass()).warn("Failed to get user", e);
					}
				}
			}
		}
			
		if (json.has("ownerRegion"))
			this.ownerRegion = json.getString("ownerRegion");

		if (json.has("creationDate"))
			this.creationDate = ZonedDateTime.parse(json.getString("creationDate"));

		if (json.has("description"))
			this.description = json.getString("description");
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getOwnerRegion() {
		return ownerRegion;
	}

	public void setOwnerRegion(String ownerRegion) {
		this.ownerRegion = ownerRegion;
	}

	public ZonedDateTime getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(ZonedDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<DangerSourceVariant> getDangerSourceVariants() {
		return this.dangerSourceVariants;
	}

	public void setDangerSourceVariants(Set<DangerSourceVariant> dangerSourceVariants) {
		this.dangerSourceVariants = dangerSourceVariants;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (!Strings.isNullOrEmpty(id))
			json.put("id", id);

		if (user != null && !Strings.isNullOrEmpty(user.getName()))
			json.put("author", user.toSmallJSON());

		if (user != null && user.getRoles() != null)
			json.put("ownerRegion", ownerRegion);

		return json;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!DangerSource.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final DangerSource other = (DangerSource) obj;

		return true;
	}

}
