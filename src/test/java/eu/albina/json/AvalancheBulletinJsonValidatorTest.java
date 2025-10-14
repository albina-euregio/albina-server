// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.json;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import eu.albina.model.AvalancheBulletinTest;
import eu.albina.util.JsonUtil;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;

@MicronautTest
public class AvalancheBulletinJsonValidatorTest {

	@Inject
	ObjectMapper objectMapper;

	@Test
	public void testValidateAvalancheBulletinJSONValid() throws IOException {
		final URL resource = Resources.getResource("validBulletin.json");
		final String validBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		Assertions.assertEquals(Set.of(), JsonValidator.validateAvalancheBulletin(validBulletinStringFromResource));
	}

	@Test
	public void testValidateAvalancheBulletinValid() throws IOException {
		final URL resource = Resources.getResource("2019-01-16.json");
		for (AvalancheBulletin bulletin : AvalancheBulletinTest.readBulletinsUsingJackson(resource)) {
			final String json = objectMapper.cloneWithViewClass(JsonUtil.Views.Internal.class).writeValueAsString(bulletin);
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
