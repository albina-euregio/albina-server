// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.nio.charset.StandardCharsets;

import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import eu.albina.RegionTestUtils;

public class RegionTest {
	@BeforeEach
	void setUp() {
		JsonAssert.setOptions(Option.IGNORING_ARRAY_ORDER, Option.IGNORING_EXTRA_FIELDS);
	}

	@Test
	public void testCreateObjectFromJSONAndBack() throws Exception {
		final String expected = Resources.toString(Resources.getResource("region_AT-07.json"), StandardCharsets.UTF_8);
		Region region = RegionTestUtils.readRegion(Resources.getResource("region_AT-07.json"));
		JsonAssert.assertJsonEquals(expected, region.toJSON());
	}

	@Test
	public void testCreateObjectFromJSONAndBackJackson() throws Exception {
		String expected = Resources.toString(Resources.getResource("region_AT-07.json"), StandardCharsets.UTF_8);
		ObjectMapper objectMapper = new ObjectMapper();
		Region region = objectMapper.readValue(expected, Region.class);
		String json = objectMapper.writeValueAsString(region);
		JsonAssert.assertJsonEquals(expected, json);
	}
}
