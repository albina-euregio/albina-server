/*******************************************************************************
 * Copyright (C) 2025 albina
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
@Table(name = "whatsapp_configurations")
public class WhatsAppConfiguration implements Serializable {

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
		return lang;
	}

	public WhatsAppConfiguration languageCode(LanguageCode languageCode) {
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
