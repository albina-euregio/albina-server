package eu.albina.model.enumerations;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

import eu.albina.util.GlobalVariables;

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
				System.out.println(rating + " = " + GlobalVariables.getDangerRatingTextShort(rating, languageCode));
				System.out.println(rating + ".long = " + GlobalVariables.getDangerRatingTextMiddle(rating, languageCode));
			}
		}
	}
}
