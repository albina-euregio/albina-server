// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication;

import java.io.Serializable;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rapid_mail_configurations")
public class RapidMailConfiguration implements Serializable {

    public static final String TECH_SUBJECT_MATTER = "tech";
    private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	private Region region;

	@Column(name = "USERNAME", length = 191)
	private String username;

	@Column(name = "PASSWORD",  length = 191)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE",  length = 191)
	private LanguageCode lang;

	@Column(name = "MAILINGLIST_NAME", length = 191)
	private String mailinglistName;

	@Column(name = "SUBJECT_MATTER", length = 191)
	private String subjectMatter;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LanguageCode getLang() {
		return lang;
	}

	public void setLang(LanguageCode lang) {
		this.lang = lang;
	}

	public String getMailinglistName() {
		return mailinglistName;
	}

	public void setMailinglistName(String mailinglistName) {
		this.mailinglistName = mailinglistName;
	}

	public String getSubjectMatter() {
		return subjectMatter;
	}

	public void setSubjectMatter(String subjectMatter) {
		this.subjectMatter = subjectMatter;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("region", region)
			.add("username", username)
			.add("password", password)
			.add("lang", lang)
			.add("mailinglistName", mailinglistName)
			.add("subjectMatter", subjectMatter)
			.toString();
	}
}
