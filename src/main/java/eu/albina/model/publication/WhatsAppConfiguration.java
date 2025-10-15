// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication;

import java.io.Serializable;
import java.util.Objects;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import io.micronaut.serde.annotation.Serdeable;
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
@Table(name = "whatsapp_configurations")
@Serdeable
public class WhatsAppConfiguration implements Serializable {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	private Region region;

	@Column(name = "API_TOKEN", length = 191)
	private String apiToken;

	@Column(name = "CHAT_ID",  length = 191)
	private String chatId;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE", length = 191)
	private LanguageCode languageCode;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Region getRegion() {
		return region;
	}

	public WhatsAppConfiguration region(Region region) {
		this.region = region;
		return this;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public String getApiToken() {
		return apiToken;
	}

	public WhatsAppConfiguration apiToken(String apiToken) {
		this.apiToken = apiToken;
		return this;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public String getChatId() {
		return chatId;
	}

	public WhatsAppConfiguration chatId(String chatId) {
		this.chatId = chatId;
		return this;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public LanguageCode getLanguageCode() {
		return languageCode;
	}

	public WhatsAppConfiguration languageCode(LanguageCode languageCode) {
		this.languageCode = languageCode;
		return this;
	}

	public void setLanguageCodeId(LanguageCode languageCode) {
		this.languageCode = languageCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		WhatsAppConfiguration whatsAppConfig = (WhatsAppConfiguration) o;
		if (whatsAppConfig.getId() == null || getId() == null) {
			return false;
		}
		return Objects.equals(getId(), whatsAppConfig.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "WhatsAppConfig{" + "id=" + getId() + ", apiToken='" + getApiToken() + "'" + ", chatId='" + getChatId()
				+ "'" + ", lanugageCode='" + getLanguageCode() + "'" + "}";
	}
}
