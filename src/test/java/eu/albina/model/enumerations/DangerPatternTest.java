package eu.albina.model.enumerations;

import java.util.Locale;

import static org.junit.Assert.*;
import org.junit.Test;

public class DangerPatternTest {
	@Test
	public void testI18n() {
		assertEquals("gm 7: schneearm neben schneereich", DangerPattern.dp7.toString(Locale.GERMAN));
		assertEquals("md7: zones avec peu de neige \u00E0 cot\u00E9 des zones enneig\u00E9es",
			DangerPattern.dp7.toString(Locale.FRENCH));
	}
}
