// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication;

import java.io.Serializable;
import java.util.Objects;

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
@Table(name = "telegram_configurations")
public class TelegramConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	private Region region;

	@Column(name = "API_TOKEN", length = 191)
	private String apiToken;

	@Column(name = "CHAT_ID", length = 191)
	private String chatId;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE", length = 191)
	private LanguageCode lang;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Region getRegion() {
		return region;
	}

	public TelegramConfiguration region(Region region) {
		this.region = region;
		return this;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public String getApiToken() {
		return apiToken;
	}

	public TelegramConfiguration apiToken(String apiToken) {
		this.apiToken = apiToken;
		return this;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public String getChatId() {
		return chatId;
	}

	public TelegramConfiguration chatId(String chatId) {
		this.chatId = chatId;
		return this;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public LanguageCode getLanguageCode() {
		return lang;
	}

	public TelegramConfiguration languageCode(LanguageCode languageCode) {
		this.lang = languageCode;
		return this;
	}

	public void setLanguageCodeId(LanguageCode languageCode) {
		this.lang = languageCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TelegramConfiguration telegramConfig = (TelegramConfiguration) o;
		if (telegramConfig.getId() == null || getId() == null) {
			return false;
		}
		return Objects.equals(getId(), telegramConfig.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "TelegramConfig{" + "id=" + getId() + ", apiToken='" + getApiToken() + "'" + ", chatId='" + getChatId()
				+ "'" + ", lanugageCode='" + getLanguageCode() + "'" + "}";
	}
}
