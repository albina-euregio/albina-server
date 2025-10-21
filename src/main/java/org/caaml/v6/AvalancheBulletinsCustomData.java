// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class AvalancheBulletinsCustomData {
	public final AvalancheBulletinsCustomData.ALBINA ALBINA;

	public AvalancheBulletinsCustomData(AvalancheBulletinsCustomData.ALBINA ALBINA) {
		this.ALBINA = ALBINA;
	}

	@Serdeable
	public static class ALBINA {
		public final String generalHeadline;

		public ALBINA(String generalHeadline) {
			this.generalHeadline = generalHeadline;
		}
	}
}
