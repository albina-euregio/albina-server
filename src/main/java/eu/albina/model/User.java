package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.model.enumerations.Role;

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

	@Enumerated(EnumType.STRING)
	@Column(name = "ROLE")
	private Role role;

	/** Image of the user **/
	@Column(name = "IMAGE", columnDefinition = "LONGBLOB")
	private String image;

	/**
	 * Standard constructor for a user.
	 */
	public User() {
	}

	public User(JSONObject json) {
		if (json.has("email") && !json.isNull("email"))
			this.email = json.getString("email");
		if (json.has("password") && !json.isNull("password"))
			this.password = json.getString("password");
		if (json.has("name") && !json.isNull("name"))
			this.name = json.getString("name");
		if (json.has("role") && !json.isNull("role"))
			this.role = Role.fromString(json.getString("role"));
		if (json.has("image") && !json.isNull("image"))
			this.image = json.getString("image");
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
		this.email = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	// TODO implement correct operation
	public Element toCAAML(Document doc) {
		Element operation = doc.createElement("Operation");
		operation.setAttribute("gml:id", "OpIDLwdTirol");
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode("Lawinenwarndienst Tirol"));
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