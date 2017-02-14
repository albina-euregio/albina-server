package org.avalanches.ais.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.json.JSONObject;

@Entity
@Table(name = "AUTHORS")
public class Author extends AbstractPersistentObject implements AvalancheInformationObject {

	/** The name of the author */
	@Column(name = "NAME")
	private String name;

	/** Email address of the author or the organization */
	@Column(name = "EMAIL")
	private String email;

	/** Phone number of the author or the organization */
	@Column(name = "PHONE")
	private String phone;

	/** Name of the organization (avalanche warning service) */
	@Column(name = "ORGANIZATION")
	private String organization;

	/** Role of the author */
	@Column(name = "ROLE")
	private String role;

	/** Additional data in JSON format */
	@Lob
	@Column(name = "CUSTOM_DATA")
	private String customData;

	/**
	 * Standard constructor for an author.
	 */
	public Author() {
	}

	public Author(JSONObject json) {
		if (json.has("name") && !json.isNull("name"))
			this.name = json.getString("name");
		if (json.has("email") && !json.isNull("email"))
			this.email = json.getString("email");
		if (json.has("phone") && !json.isNull("phone"))
			this.phone = json.getString("phone");
		if (json.has("organization") && !json.isNull("organization"))
			this.organization = json.getString("organization");
		if (json.has("role") && !json.isNull("role"))
			this.role = json.getString("role");
		if (json.has("customData") && !json.isNull("customData"))
			this.customData = json.getString("customData");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getCustomData() {
		return customData;
	}

	public void setCustomData(String customData) {
		this.customData = customData;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (name != null && name != "")
			json.put("name", name);
		if (email != null && email != "")
			json.put("email", email);
		if (phone != null && phone != "")
			json.put("phone", phone);
		if (organization != null && organization != "")
			json.put("organization", organization);
		if (role != null && role != "")
			json.put("role", role);
		if (customData != null && customData != "")
			json.put("customData", customData);

		return json;
	}

}
