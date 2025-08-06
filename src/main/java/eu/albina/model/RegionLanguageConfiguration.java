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
package eu.albina.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import eu.albina.model.enumerations.LanguageCode;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@IdClass(RegionLanguageConfiguration.RegionLanguageConfigurationID.class)
@Table(name = "region_language_configurations")
public class RegionLanguageConfiguration {

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REGION_ID")
	@JsonIgnore
	private Region region;

	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE", length = 191)
	private LanguageCode lang;

	@Column(name = "WEBSITE_NAME", length = 191)
	private String websiteName;

	@Column(name = "WARNING_SERVICE_NAME", length = 191)
	private String warningServiceName;

	@Column(name = "WARNING_SERVICE_EMAIL", length = 191)
	private String warningServiceEmail;

	@Column(name = "URL", length = 191)
	private String url;

	@Column(name = "URL_WITH_DATE", length = 191)
	private String urlWithDate;

	@Column(name = "STATIC_URL", length = 191)
	private String staticUrl;

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public LanguageCode getLang() {
		return lang;
	}

	public void setLang(LanguageCode lang) {
		this.lang = lang;
	}

	public String getWebsiteName() {
		return websiteName;
	}

	public void setWebsiteName(String websiteName) {
		this.websiteName = websiteName;
	}

	public String getWarningServiceName() {
		return warningServiceName;
	}

	public void setWarningServiceName(String warningServiceName) {
		this.warningServiceName = warningServiceName;
	}

	public String getWarningServiceEmail() {
		return warningServiceEmail;
	}

	public void setWarningServiceEmail(String warningServiceEmail) {
		this.warningServiceEmail = warningServiceEmail;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlWithDate() {
		return urlWithDate;
	}

	public void setUrlWithDate(String urlWithDate) {
		this.urlWithDate = urlWithDate;
	}

	public String getStaticUrl() {
		return staticUrl;
	}

	public void setStaticUrl(String staticUrl) {
		this.staticUrl = staticUrl;
	}

	public static final class RegionLanguageConfigurationID {
		private Region region;
		private LanguageCode lang;

		public RegionLanguageConfigurationID() {
		}

		public RegionLanguageConfigurationID(Region region, LanguageCode lang) {
			this.region = region;
			this.lang = lang;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RegionLanguageConfiguration.RegionLanguageConfigurationID that = (RegionLanguageConfiguration.RegionLanguageConfigurationID) o;
			return Objects.equals(region, that.region) && Objects.equals(lang, that.lang);
		}

		@Override
		public int hashCode() {
			return Objects.hash(region, lang);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("region", region)
				.add("lang", lang)
				.toString();
		}
	}
}
