package eu.albina.model.enumerations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DangerPatternTest {
	@Test
	public void testI18n() {
		Assertions.assertEquals("gm.7: schneearm neben schneereich", DangerPattern.dp7.toString(Locale.GERMAN));
		Assertions.assertEquals("md.7: zones avec peu de neige à coté des zones enneigées", DangerPattern.dp7.toString(Locale.FRENCH));
	}
}
