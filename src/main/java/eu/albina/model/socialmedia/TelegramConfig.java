/*******************************************************************************
 * Copyright (C) 2020 Avalanche.report
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
package eu.albina.model.socialmedia;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.albina.model.enumerations.LanguageCode;

/**
 * A TelegramConfig.
 */
@Entity
@Table(name = "socialmedia_telegram_config")
// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
// property = "id",scope = TwitterConfig.class)
public class TelegramConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "API_TOKEN")
	private String apiToken;

	@Column(name = "CHAT_ID")
	private String chatId;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE")
	private LanguageCode lang;

	@ManyToOne
	@JoinColumn(name = "PROVIDER_ID")
	private Provider provider;

	@JsonIgnoreProperties(value = { "region", "twitterConfig", "rapidMailConfig",
			"telegramConfigs", "shipments", "channels" }, allowSetters = true)
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(unique = true, name = "REGION_CONFIGURATION_ID")
	private RegionConfiguration regionConfiguration;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getApiToken() {
		return apiToken;
	}

	public TelegramConfig apiToken(String apiToken) {
		this.apiToken = apiToken;
		return this;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public String getChatId() {
		return chatId;
	}

	public TelegramConfig chatId(String chatId) {
		this.chatId = chatId;
		return this;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public LanguageCode getLanguageCode() {
		return lang;
	}

	public TelegramConfig languageCode(LanguageCode languageCode) {
		this.lang = languageCode;
		return this;
	}

	public void setLanguageCodeId(LanguageCode languageCode) {
		this.lang = languageCode;
	}

	public RegionConfiguration getRegionConfiguration() {
		return regionConfiguration;
	}

	public TelegramConfig regionConfiguration(RegionConfiguration regionConfiguration) {
		this.regionConfiguration = regionConfiguration;
		return this;
	}

	public void setRegionConfiguration(RegionConfiguration regionConfiguration) {
		this.regionConfiguration = regionConfiguration;
	}

	public Provider getProvider() {
		return provider;
	}

	public TelegramConfig provider(Provider provider) {
		this.provider = provider;
		return this;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TelegramConfig telegramConfig = (TelegramConfig) o;
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
