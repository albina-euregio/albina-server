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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;

@Entity
@Table(name = "users")
public class User {

	/** Email address of the user */
	@Id
	@Column(name = "EMAIL", length = 191)
	private String email;

	/** Password of the user */
	@Column(name = "PASSWORD", length = 191)
	private String password;

	/** Name of the user **/
	@Column(name = "NAME",  length = 191)
	private String name;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "USER_EMAIL"))
	@Column(name = "USER_ROLE", length = 191)
	@Enumerated(EnumType.STRING)
	private List<Role> roles;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="user_region",
	 joinColumns=@JoinColumn(name="USER_EMAIL"),
	 inverseJoinColumns=@JoinColumn(name="REGION_ID")
	)
	private Set<Region> regions;

	/** Image of the user **/
	@Column(name = "IMAGE", columnDefinition = "LONGBLOB")
	private String image;

	/** Organization the user works for **/
	@Column(name = "ORGANIZATION", length = 191)
	private String organization;

	/** The avalanche bulletins of the user */
	@OneToMany(mappedBy = "user")
	private List<AvalancheBulletin> bulletins;

	/** Prefered language of the user */
	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE", length = 191)
	private LanguageCode languageCode;

	@Column(name = "DELETED")
	private boolean deleted;

	/**
	 * Standard constructor for a user.
	 */
	public User() {
		regions = new HashSet<Region>();
		roles = new ArrayList<Role>();
	}

	public User(String email) {
		this.email = email;
	}

	public User(JSONObject json, Function<String, Region> regionFunction) {
		this();
		if (json.has("email") && !json.isNull("email"))
			this.email = json.getString("email");
		if (json.has("password") && !json.isNull("password"))
			this.password = json.getString("password");
		if (json.has("name") && !json.isNull("name"))
			this.name = json.getString("name");
		if (json.has("image") && !json.isNull("image"))
			this.image = json.getString("image");
		if (json.has("organization") && !json.isNull("organization"))
			this.organization = json.getString("organization");
		if (json.has("regions")) {
			JSONArray regions = json.getJSONArray("regions");
			for (Object region : regions) {
				this.regions.add(regionFunction.apply((String) region));
			}
		}
		if (json.has("roles")) {
			JSONArray roles = json.getJSONArray("roles");
			for (Object role : roles) {
				this.roles.add(Role.fromString((String) role));
			}
		}
		if (json.has("languageCode") && !json.isNull("languageCode"))
			this.languageCode = LanguageCode.valueOf((json.getString("languageCode").toLowerCase()));
		if (json.has("deleted") && !json.isNull("deleted"))
			this.deleted = json.getBoolean("deleted");
	}

	public List<AvalancheBulletin> getBulletins() {
		return bulletins;
	}

	public void setBulletins(List<AvalancheBulletin> bulletins) {
		this.bulletins = bulletins;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public void addRole(Role role) {
		if (!this.roles.contains(role))
			this.roles.add(role);
	}

	public Set<Region> getRegions() {
		return regions;
	}

	public void setRegions(Set<Region> regions) {
		this.regions = regions;
	}

	public void addRegion(Region region) {
		if (!this.regions.contains(region))
			this.regions.add(region);
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public LanguageCode getLanguage() {
		return languageCode;
	}

	public void setLanguage(LanguageCode language) {
		this.languageCode = language;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public JSONObject toJSON() throws JsonProcessingException {
		JSONObject json = new JSONObject();

		json.put("email", getEmail());
		json.put("name", getName());
		json.put("image", getImage());
		json.put("organization", getOrganization());

		if (roles != null && roles.size() > 0) {
			JSONArray jsonRoles = new JSONArray();
			for (Role role : roles) {
				jsonRoles.put(role.toString());
			}
			json.put("roles", jsonRoles);
		}

		if (regions != null && regions.size() > 0) {
			JSONArray jsonRegions = new JSONArray();
			for (Region region : regions) {
				jsonRegions.put(new JSONObject(region.toJSON()));
			}
			json.put("regions", jsonRegions);
		}

		if (languageCode != null)
			json.put("languageCode", this.languageCode.toString());

		json.put("deleted", isDeleted());

		return json;
	}

	public JSONObject toSmallJSON() {
		JSONObject json = new JSONObject();

		json.put("email", getEmail());
		json.put("name", getName());

		return json;
	}

	public Element toCAAML(Document doc) {
		Element operation = doc.createElement("Operation");
		operation.setAttribute("gml:id", this.organization);
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(this.organization));
		operation.appendChild(name);
		Element contactPerson = doc.createElement("contactPerson");
		Element person = doc.createElement("Person");
		person.setAttribute("gml:id", email);
		Element personName = doc.createElement("name");
		personName.appendChild(doc.createTextNode(this.name));
		person.appendChild(personName);
		contactPerson.appendChild(person);
		operation.appendChild(contactPerson);
		return operation;
	}

	public boolean hasPermissionForRegion(String regionId) {
		return getRegions().stream().anyMatch(region -> region.getId().equals(regionId));
	}

	public boolean hasRole(Role role) {
		return getRoles().stream().anyMatch(userRole -> userRole.equals(role));
	}

	@Override
	public String toString() {
		return email;
	}
}
