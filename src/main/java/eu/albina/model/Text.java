// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

import eu.albina.model.enumerations.LanguageCode;

import java.util.Comparator;

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Text(LanguageCode languageCode, String text) implements Comparable<Text> {

	private static final Comparator<Text> COMPARATOR = Comparator.comparing(Text::languageCode);

	@Override
	public int compareTo(Text o) {
		return COMPARATOR.compare(this, o);
	}
}
