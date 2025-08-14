// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.json;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;

public class AvalancheBulletinJsonValidatorTest {

	@Test
	public void testValidateAvalancheBulletinJSONValid() throws IOException {
		final URL resource = Resources.getResource("validBulletin.json");
		final String validBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		Assertions.assertEquals(0, JsonValidator.validateAvalancheBulletin(validBulletinStringFromResource).size());
	}

	@Test
	public void testValidateAvalancheBulletinValid() throws IOException {
		final URL resource = Resources.getResource("2019-01-16.json");
		for (AvalancheBulletin bulletin : AvalancheBulletin.readBulletins(resource)) {
			final String json = bulletin.toJSON().toString();
			Assertions.assertEquals(0, JsonValidator.validateAvalancheBulletin(json).size());
		}
	}

	@Test
	public void testValidateAvalancheBulletinJSONInvalid() throws IOException {
		final URL resource = Resources.getResource("invalidBulletin.json");
		final String invalidBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		Assertions.assertEquals(3, JsonValidator.validateAvalancheBulletin(invalidBulletinStringFromResource).size());
	}

}
