// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CaamlBundleSmokeTest {
	@Test
	public void testNewEnumsReadFromCaamlBundle() {
		Assertions.assertEquals("schlecht", SnowpackStability.poor.toString(Locale.GERMAN));
		Assertions.assertEquals("poor", SnowpackStability.poor.toString(Locale.ENGLISH));
		Assertions.assertEquals("groß", AvalancheSize.large.toString(Locale.GERMAN));
		Assertions.assertEquals("large", AvalancheSize.large.toString(Locale.ENGLISH));
		Assertions.assertEquals("wenige", Frequency.few.toString(Locale.GERMAN));
		Assertions.assertEquals("few", Frequency.few.toString(Locale.ENGLISH));
	}

	@Test
	public void testMigratedEnumsStillReadFromCaamlBundle() {
		Assertions.assertEquals("Nord", Aspect.N.toString(Locale.GERMAN));
		Assertions.assertEquals("Neuschnee", AvalancheProblem.new_snow.toString(Locale.GERMAN));
		Assertions.assertEquals("Tendenz: Lawinengefahr nimmt ab", Tendency.decreasing.toString(Locale.GERMAN));
		Assertions.assertEquals("gm.1: bodennahe schwachschicht", DangerPattern.dp1.toString(Locale.GERMAN));
		Assertions.assertEquals("erheblich", DangerRating.considerable.toString(Locale.GERMAN, false));
		Assertions.assertEquals("Gefahrenstufe 3 - Erheblich", DangerRating.considerable.toString(Locale.GERMAN, true));
	}
}
