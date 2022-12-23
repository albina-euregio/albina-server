package eu.albina.model.enumerations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DangerRatingTest {
	@Test
	public void testI18n() {
		Assertions.assertEquals("erheblich", DangerRating.considerable.toString(Locale.GERMAN, false));
		Assertions.assertEquals("Gefahrenstufe 3 - Erheblich", DangerRating.considerable.toString(Locale.GERMAN, true));
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
