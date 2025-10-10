// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import eu.albina.model.converter.EnumSetToStringConverter;

public enum TextcatTextfield {
	avActivityComment,
	avActivityHighlights,
	generalHeadlineComment,
	highlights,
	snowpackStructureComment,
	snowpackStructureHighlights,
	synopsisComment,
	tendencyComment,
	;

	@jakarta.persistence.Converter
	public static class Converter extends EnumSetToStringConverter<TextcatTextfield> {

		public static final String COLUMN_DEFINITION = "set('avActivityComment','avActivityHighlights','generalHeadlineComment','highlights','snowpackStructureComment','snowpackStructureHighlights','synopsisComment','tendencyComment')";

		@Override
		protected Class<TextcatTextfield> getEnumClass() {
			return TextcatTextfield.class;
		}
	}
}
