package eu.albina.model.enumerations;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

public class DangerRatingTest {
	@Test
	public void testI18n() {
		assertEquals("erheblich", DangerRating.considerable.toString(Locale.GERMAN, false));
		assertEquals("Gefahrenstufe 3 - Erheblich", DangerRating.considerable.toString(Locale.GERMAN, true));
	}

	@Test
	@Ignore
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
