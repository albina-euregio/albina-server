// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@JsonPropertyOrder(alphabetic = true)
public class AvalancheBulletinCustomData {
	private final ALBINA ALBINA;
	private final LwdTyrol LWD_Tyrol;

	public AvalancheBulletinCustomData(ALBINA ALBINA, LwdTyrol LWD_Tyrol) {
		this.ALBINA = ALBINA;
		this.LWD_Tyrol = LWD_Tyrol;
	}

	@JacksonXmlProperty(localName = "ALBINA")
	public ALBINA getALBINA() {
		return ALBINA;
	}

	@JacksonXmlProperty(localName = "LWD_Tyrol")
	public LwdTyrol getLWD_Tyrol() {
		return LWD_Tyrol;
	}

	@Serdeable
	public static class ALBINA {
		public final String mainDate;

		@JacksonXmlElementWrapper(useWrapping = false)
		@JacksonXmlProperty(localName = "bulletinPhoto")
		public final List<BulletinPhoto> bulletinPhotos;

		public ALBINA(String mainDate, List<BulletinPhoto> bulletinPhotos) {
			this.mainDate = mainDate;
			this.bulletinPhotos = bulletinPhotos;
		}

		public String getMainDate() {
			return mainDate;
		}

		public List<BulletinPhoto> getBulletinPhotos() {
			return bulletinPhotos;
		}
	}

	@Serdeable
	public static class LwdTyrol {
		@JacksonXmlElementWrapper(useWrapping = false)
		@JacksonXmlProperty(localName = "dangerPatterns")
		public final List<String> dangerPatterns;

		public LwdTyrol(List<String> dangerPatterns) {
			this.dangerPatterns = dangerPatterns;
		}

		public List<String> getDangerPatterns() {
			return dangerPatterns;
		}
	}

	@Serdeable
	public static class BulletinPhoto {
		public final String url;
		public final String copyright;
		public final java.time.LocalDate date;
		public final String microRegionId;

		public BulletinPhoto(String url, String copyright, java.time.LocalDate date, String microRegionId) {
			this.url = url;
			this.copyright = copyright;
			this.date = date;
			this.microRegionId = microRegionId;
		}

		public String getUrl() {
			return url;
		}

		public String getCopyright() {
			return copyright;
		}

		public java.time.LocalDate getDate() {
			return date;
		}

		public String getMicroRegionId() {
			return microRegionId;
		}
	}
}
