package org.caaml.v6;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class AvalancheProblemCustomData {
	public final ALBINA ALBINA;

	public AvalancheProblemCustomData(ALBINA ALBINA) {
		this.ALBINA = ALBINA;
	}

	public static class ALBINA {
		@JacksonXmlElementWrapper(useWrapping = false)
		@JacksonXmlProperty(localName = "avalancheType")
		public final String avalancheType;

		public ALBINA(String avalancheType) {
			this.avalancheType = avalancheType;
		}
	}
}
