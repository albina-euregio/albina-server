// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class AvalancheBulletinCustomData {
	public final ALBINA ALBINA;
	public final LwdTyrol LWD_Tyrol;

	public AvalancheBulletinCustomData(ALBINA ALBINA, LwdTyrol LWD_Tyrol) {
		this.ALBINA = ALBINA;
		this.LWD_Tyrol = LWD_Tyrol;
	}

	public static class ALBINA {
		public final String mainDate;

		public ALBINA(String mainDate) {
			this.mainDate = mainDate;
		}
	}

	public static class LwdTyrol {
		@JacksonXmlElementWrapper(useWrapping = false)
		@JacksonXmlProperty(localName = "dangerPatterns")
		public final List<String> dangerPatterns;

		public LwdTyrol(List<String> dangerPatterns) {
			this.dangerPatterns = dangerPatterns;
		}
	}
}
