// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.caaml;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.google.common.io.Resources;

public class CaamlValidatorTest {

	@Test
	public void testValidateAvalancheBulletinCaamlValid() throws Exception {
		final URL resource = Resources.getResource("caaml/validBulletin.xml");
		final String validBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		CaamlValidator.validateCaamlBulletin(validBulletinStringFromResource, CaamlVersion.V5);
	}

	@Test
	public void testValidateAvalancheBulletinCaamlInvalid() throws SAXException, IOException {
		Assertions.assertThrows(SAXException.class, () -> {
			final URL resource = Resources.getResource("caaml/invalidBulletin.xml");
			final String invalidBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
			CaamlValidator.validateCaamlBulletin(invalidBulletinStringFromResource, CaamlVersion.V5);
		});
	}

	@Test
	public void testValidateAvalancheBulletinCaamlUnclear() throws Exception {
		final URL resource = Resources.getResource("caaml/unclearBulletin.xml");
		final String unclearBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		CaamlValidator.validateCaamlBulletin(unclearBulletinStringFromResource, CaamlVersion.V5);
	}

}
