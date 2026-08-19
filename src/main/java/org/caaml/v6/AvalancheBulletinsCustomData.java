// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record AvalancheBulletinsCustomData(ALBINA ALBINA) {

	@Serdeable
	public record ALBINA(String generalHeadline) {
	}
}
