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
		Assertions.assertEquals("Gefahrenstufe 3 — Erheblich", DangerRating.considerable.toString(Locale.GERMAN, true));
	}

	@Test
	public void testValidTimePeriodReadsFromCaamlBundle() {
		Assertions.assertEquals("früher", LanguageCode.de.getCaamlBundleString("validTimePeriod.earlier"));
		Assertions.assertEquals("Später am Tag", LanguageCode.de.getCaamlBundleString("validTimePeriod.later.long"));
		Assertions.assertEquals("earlier", LanguageCode.en.getCaamlBundleString("validTimePeriod.earlier"));
	}

	@Test
	public void testLabelsReadFromCaamlBundle() {
		Assertions.assertEquals("Tendenz", LanguageCode.de.getCaamlBundleString("tendency.label"));
		Assertions.assertEquals("Wetter", LanguageCode.de.getCaamlBundleString("synopsis.label"));
		Assertions.assertEquals("Gefahrenmuster", LanguageCode.de.getCaamlBundleString("dangerPattern.label"));
		Assertions.assertEquals("Schneedecke", LanguageCode.de.getCaamlBundleString("snowpack.label"));
		Assertions.assertEquals("Travel Advisory", LanguageCode.de.getCaamlBundleString("travelAdvisory.label"));
		Assertions.assertEquals("Lawinenproblem", LanguageCode.de.getCaamlBundleString("avalancheProblem.label"));
		Assertions.assertEquals("Gefahrenstufe", LanguageCode.de.getCaamlBundleString("dangerRating.label"));
		Assertions.assertEquals("Lawinenvorhersage", LanguageCode.de.getCaamlBundleString("forecast.label"));
		Assertions.assertEquals("Avalanche Forecast", LanguageCode.en.getCaamlBundleString("forecast.label"));
		Assertions.assertEquals("Schneedeckenstabilität", LanguageCode.de.getCaamlBundleString("snowpackStability.label"));
		Assertions.assertEquals("Gefahrenstellen", LanguageCode.de.getCaamlBundleString("frequency.label"));
		Assertions.assertEquals("Lawinengröße", LanguageCode.de.getCaamlBundleString("avalancheSize.label"));
		Assertions.assertEquals("Snowpack stability", LanguageCode.en.getCaamlBundleString("snowpackStability.label"));
	}

	@Test
	public void testElevationReadsFromCaamlBundle() {
		Assertions.assertEquals("Waldgrenze", LanguageCode.de.getCaamlBundleString("elevation.treeline.capitalized"));
		Assertions.assertEquals("Treeline", LanguageCode.en.getCaamlBundleString("elevation.treeline.capitalized"));
		Assertions.assertEquals("treeline", LanguageCode.en.getCaamlBundleString("elevation.treeline"));
		Assertions.assertEquals("Waldgrenze", LanguageCode.de.getCaamlBundleString("elevation.treeline"));
		Assertions.assertEquals("All elevations", LanguageCode.en.getCaamlBundleString("elevation.all"));
		Assertions.assertEquals("Unter {0}", LanguageCode.de.getCaamlBundleString("elevation.below"));
		Assertions.assertEquals("Above {0}", LanguageCode.en.getCaamlBundleString("elevation.above"));
		Assertions.assertEquals("Zwischen {0} und {1}", LanguageCode.de.getCaamlBundleString("elevation.band"));
	}
}
