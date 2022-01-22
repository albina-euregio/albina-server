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
package eu.albina.model.publication;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;

@Entity
@Table(name = "telegram_configurations")
public class TelegramConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "PROVIDER_ID")
	private PublicationProvider provider;

	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	private Region region;
	@Column(name = "API_TOKEN")
	private String apiToken;

	@Column(name = "CHAT_ID")
	private String chatId;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE")
	private LanguageCode lang;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PublicationProvider getProvider() {
		return provider;
	}

	public TelegramConfiguration provider(PublicationProvider provider) {
		this.provider = provider;
		return this;
	}

	public void setProvider(PublicationProvider provider) {
		this.provider = provider;
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
	}	public String getApiToken() {
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
