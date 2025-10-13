// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class AvalancheProblemCustomData {
	public final ALBINA ALBINA;

	public AvalancheProblemCustomData(ALBINA ALBINA) {
		this.ALBINA = ALBINA;
	}

	public ALBINA getALBINA() {
		return ALBINA;
	}

	@Serdeable
	public static class ALBINA {
		@JacksonXmlElementWrapper(useWrapping = false)
		@JacksonXmlProperty(localName = "avalancheType")
		public final String avalancheType;

		public ALBINA(String avalancheType) {
			this.avalancheType = avalancheType;
		}

		public String getAvalancheType() {
			return avalancheType;
		}
	}
}
