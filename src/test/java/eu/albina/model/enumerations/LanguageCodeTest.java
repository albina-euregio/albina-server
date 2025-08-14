// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LanguageCodeTest {

	@Test
	public void retrieveTranslationTest() {
		String string = LanguageCode.ca.getBundleString("headline.tendency");
		Assertions.assertEquals("Tendència", string);
	}

}
