package eu.albina.model.enumerations;

import java.util.Locale;

import static org.junit.Assert.*;
import org.junit.Test;

public class DangerPatternTest {
	@Test
	public void testI18n() {
		assertEquals("gm 7: schneearm neben schneereich", DangerPattern.dp7.toString(Locale.GERMAN));
		assertEquals("gm 7: schneearm neben schneereich", DangerPattern.dp7.toString(Locale.FRENCH)); // until fr is translated
	}
}
