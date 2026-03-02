// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import eu.albina.model.converter.EnumSetToStringConverter;

public enum DangerSign {
	shooting_cracks, whumpfing, fresh_avalanches, glide_cracks;

	@jakarta.persistence.Converter
	public static class Converter extends EnumSetToStringConverter<DangerSign> {

		public static final String COLUMN_DEFINITION = "set('shooting_cracks', 'whumpfing', 'fresh_avalanches', 'glide_cracks')";

		@Override
		protected Class<DangerSign> getEnumClass() {
			return DangerSign.class;
		}
	}
}
