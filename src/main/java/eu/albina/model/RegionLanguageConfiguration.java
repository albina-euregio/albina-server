// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

import eu.albina.model.enumerations.LanguageCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
