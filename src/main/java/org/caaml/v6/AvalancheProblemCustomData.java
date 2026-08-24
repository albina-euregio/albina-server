// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record AvalancheProblemCustomData(
	@JacksonXmlProperty(localName = "ALBINA")
	ALBINA ALBINA
) {


	@Serdeable
	public record ALBINA(
		@JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "avalancheType")
		String avalancheType
	) {
	}
}
