package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.json.JSONObject;

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
}
