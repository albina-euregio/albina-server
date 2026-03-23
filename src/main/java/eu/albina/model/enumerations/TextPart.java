// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import eu.albina.model.converter.EnumSetToStringConverter;

public enum TextPart {
	generalHeadlineComment,
	highlights,
	avActivityHighlights,
	avActivityComment,
	snowpackStructureHighlights,
	snowpackStructureComment,
	synopsisHighlights,
	synopsisComment,
	tendencyComment,
	travelAdvisoryHighlights,
	travelAdvisoryComment,
	;

	@jakarta.persistence.Converter
	public static class Converter extends EnumSetToStringConverter<TextPart> {

		public static final String COLUMN_DEFINITION = """
			set(
			'avActivityComment',
			'avActivityHighlights',
			'generalHeadlineComment',
			'highlights',
			'snowpackStructureComment',
			'snowpackStructureHighlights',
			'synopsisComment',
			'synopsisHighlights',
			'tendencyComment',
			'travelAdvisoryComment',
			'travelAdvisoryHighlights'
			)""";

		@Override
		protected Class<TextPart> getEnumClass() {
			return TextPart.class;
		}
	}

}
