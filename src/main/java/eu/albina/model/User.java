package eu.albina.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.model.enumerations.Role;

@Audited
@Entity
@Table(name = "USERS")
public class User {

	/** Email address of the user */
	@Id
	@Column(name = "EMAIL")
	private String email;

	/** Password of the user */
	@Column(name = "PASSWORD")
	private String password;

	/** Name of the user **/
	@Column(name = "NAME")
	private String name;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "USER_EMAIL"))
	@Column(name = "USER_ROLE")
	@Enumerated(EnumType.STRING)
	private List<Role> roles;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "USER_REGION", joinColumns = @JoinColumn(name = "USER_EMAIL"))
	@Column(name = "USER_REGION")
	private Set<String> regions;

	/** Image of the user **/
	@Column(name = "IMAGE", columnDefinition = "LONGBLOB")
	private String image;

	/** Organization the user works for **/
	@Column(name = "ORGANIZATION")
	private String organization;

	/** The avalanche bulletins of the user */
	@OneToMany(mappedBy = "user")
	private List<AvalancheBulletin> bulletins;

	/**
	 * Standard constructor for a user.
	 */
	public User() {
		regions = new HashSet<String>();
		roles = new ArrayList<Role>();
	}

	public User(JSONObject json) {
		if (json.has("email") && !json.isNull("email"))
			this.email = json.getString("email");
		if (json.has("password") && !json.isNull("password"))
			this.password = json.getString("password");
		if (json.has("name") && !json.isNull("name"))
			this.name = json.getString("name");
		if (json.has("roles")) {
			JSONArray roles = json.getJSONArray("roles");
			for (Object entry : roles) {
				this.roles.add(Role.fromString((String) entry));
			}
		}
		if (json.has("regions")) {
			JSONArray regions = json.getJSONArray("regions");
			for (Object region : regions) {
				this.regions.add(region.toString());
			}
		}
		if (json.has("image") && !json.isNull("image"))
			this.image = json.getString("image");
		if (json.has("organization") && !json.isNull("organization"))
			this.organization = json.getString("organization");
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

	public Set<String> getRegions() {
		return regions;
	}

	public void setRegions(Set<String> regions) {
		this.regions = regions;
	}

	public void addRegion(String region) {
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

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		json.put("email", getEmail());
		json.put("name", getName());
		json.put("image", getImage());

		if (roles != null && roles.size() > 0) {
			JSONArray jsonRoles = new JSONArray();
			for (Role role : roles) {
				jsonRoles.put(role.toString());
			}
			json.put("roles", jsonRoles);
		}

		if (regions != null && regions.size() > 0) {
			JSONArray jsonRegions = new JSONArray();
			for (String region : regions) {
				jsonRegions.put(region.toString());
			}
			json.put("regions", jsonRegions);
		}

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
}
