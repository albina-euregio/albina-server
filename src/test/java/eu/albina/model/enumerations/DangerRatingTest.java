// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DangerRatingTest {
	@Test
	public void testI18n() {
		Assertions.assertEquals("erheblich", DangerRating.considerable.toString(Locale.GERMAN, false));
		Assertions.assertEquals("Gefahrenstufe 3 — Erheblich", DangerRating.considerable.toString(Locale.GERMAN, true));
	}

	@Test
	public void testLevel0() {
		Assertions.assertEquals("0", DangerRating.getString(null));
		Assertions.assertEquals("0", DangerRating.getString(DangerRating.missing));
		Assertions.assertEquals("0", DangerRating.getString(DangerRating.no_rating));
		Assertions.assertEquals("0", DangerRating.getString(DangerRating.no_snow));
		Assertions.assertEquals(0, DangerRating.getInt(null));
		Assertions.assertEquals(0, DangerRating.getInt(DangerRating.missing));
	}

	@Test
	@Disabled
	public void convert() {
		for (LanguageCode languageCode : LanguageCode.values()) {
			System.out.println();
			System.out.println(languageCode);
			for (DangerRating rating : DangerRating.values()) {
				System.out.println(rating + " = " + rating.toString(languageCode.getLocale(), false));
				System.out.println(rating + ".long = " + rating.toString(languageCode.getLocale(), true));
			}
		}
	}
}
