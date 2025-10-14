// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
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

		public ALBINA(String mainDate) {
			this.mainDate = mainDate;
		}

		public String getMainDate() {
			return mainDate;
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
}
