// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.json;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

public class SnowProfileJsonValidatorTest {

	@Disabled
	@Test
	public void testValidateSnowProfileJSONValid() throws IOException {
		final URL resource = Resources.getResource("validSnowProfile.json");
		final String validSnowProfileStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		Assertions.assertEquals(0, JsonValidator.validateSnowProfile(validSnowProfileStringFromResource).size());
	}

	@Disabled
	@Test
	public void testValidateSnowProfileJSONInvalid() throws IOException {
		final URL resource = Resources.getResource("invalidSnowProfile.json");
		final String invalidSnowProfileStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		Assertions.assertEquals(1, JsonValidator.validateSnowProfile(invalidSnowProfileStringFromResource).size());
	}

}
