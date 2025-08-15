// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.albina.util.JsonUtil;
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;

@Entity
@Table(name = "users")
@JsonView(JsonUtil.Views.Internal.class)
public class User implements NameAndEmail {

	static class UserNameSerializer extends JsonSerializer<User> {
		@Override
		public void serialize(User value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString(value.getName());
		}
	}

	/** Email address of the user */
	@Id
	@Column(name = "EMAIL", length = 191)
	@JsonView({JsonUtil.Views.Internal.class, JsonUtil.Views.Public.class})
	private String email;

	/** Password of the user */
	@Column(name = "PASSWORD", length = 191)
	@JsonIgnore
	private String password;

	/** Name of the user **/
	@Column(name = "NAME",  length = 191)
	@JsonView({JsonUtil.Views.Internal.class, JsonUtil.Views.Public.class})
	private String name;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "USER_EMAIL"))
	@Column(name = "USER_ROLE", length = 191)
	@Enumerated(EnumType.STRING)
	private Set<Role> roles = EnumSet.noneOf(Role.class);

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="user_region",
	 joinColumns=@JoinColumn(name="USER_EMAIL"),
	 inverseJoinColumns=@JoinColumn(name="REGION_ID")
	)
	@JsonSerialize(as = PersistentObject.class)
	private Set<Region> regions = new HashSet<Region>();

	/** Image of the user **/
	@Column(name = "IMAGE", columnDefinition = "LONGBLOB")
	private String image;

	/** Organization the user works for **/
	@Column(name = "ORGANIZATION", length = 191)
	private String organization;

	/** The avalanche bulletins of the user */
	@OneToMany(mappedBy = "user")
	@JsonIgnore
	private List<AvalancheBulletin> bulletins;

	/** Preferred language of the user */
	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE", length = 191)
	private LanguageCode languageCode;

	@Column(name = "DELETED")
	private boolean deleted;

	/**
	 * Standard constructor for a user.
	 */
	public User() {
	}

	public User(String email) {
		this.email = email;
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

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Set<Region> getRegions() {
		return regions;
	}

	public void setRegions(Set<Region> regions) {
		this.regions = regions;
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

	public boolean hasPermissionForRegion(String regionId) {
		return getRegions().stream().anyMatch(region -> region.getId().equals(regionId));
	}

	public boolean hasRole(Role role) {
		return getRoles().contains(role);
	}

	@Override
	public String toString() {
		return email;
	}
}
