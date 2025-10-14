// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.nio.charset.StandardCharsets;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.RegionTestUtils;

@MicronautTest
public class RegionTest {

	@Inject
	ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		JsonAssert.setOptions(Option.IGNORING_ARRAY_ORDER, Option.IGNORING_EXTRA_FIELDS);
	}

	@Test
	public void testCreateObjectFromJSONAndBack() throws Exception {
		final String expected = Resources.toString(Resources.getResource("region_AT-07.json"), StandardCharsets.UTF_8);
		Region region = RegionTestUtils.readRegion(Resources.getResource("region_AT-07.json"));
		String json = objectMapper.writeValueAsString(region);
		JsonAssert.assertJsonEquals(expected, json);
	}
}
