// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.json;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import eu.albina.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;

public class AvalancheBulletinJsonValidatorTest {

	@Test
	public void testValidateAvalancheBulletinJSONValid() throws IOException {
		final URL resource = Resources.getResource("validBulletin.json");
		final String validBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		Assertions.assertEquals(Set.of(), JsonValidator.validateAvalancheBulletin(validBulletinStringFromResource));
	}

	@Test
	public void testValidateAvalancheBulletinValid() throws IOException {
		final URL resource = Resources.getResource("2019-01-16.json");
		for (AvalancheBulletin bulletin : AvalancheBulletin.readBulletins(resource)) {
			final String json = JsonUtil.writeValueUsingJackson(bulletin, JsonUtil.Views.Internal.class);
			Assertions.assertEquals(Set.of(), JsonValidator.validateAvalancheBulletin(json));
		}
	}

	@Test
	public void testValidateAvalancheBulletinJSONInvalid() throws IOException {
		final URL resource = Resources.getResource("invalidBulletin.json");
		final String invalidBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		Assertions.assertEquals(3, JsonValidator.validateAvalancheBulletin(invalidBulletinStringFromResource).size());
	}

}
