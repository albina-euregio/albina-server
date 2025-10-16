// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.RegionTestUtils;

@MicronautTest
public class RegionTest {

	@Inject
	RegionTestUtils regionTestUtils;

	@Inject
	ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		JsonAssert.setOptions(Option.IGNORING_ARRAY_ORDER, Option.IGNORING_EXTRA_FIELDS);
	}

	@Test
	public void testCreateObjectFromJSONAndBack() throws Exception {
		final String expected = Resources.toString(Resources.getResource("region_AT-07.json"), StandardCharsets.UTF_8);
		Region region = regionTestUtils.readRegion(Resources.getResource("region_AT-07.json"));
		Assertions.assertEquals(Set.of(new Region("EUREGIO")), region.getSuperRegions());
		String json = objectMapper.writeValueAsString(region);
		JsonAssert.assertJsonEquals(expected, json);
	}

	@Test
	void testEuregioFromJson() {
		Region region = regionTestUtils.regionEuregio();
		Assertions.assertEquals("EUREGIO", region.getId());
		Assertions.assertEquals(Set.of(), region.getSuperRegions());
		Assertions.assertEquals(Set.of(new Region("AT-07"), new Region("IT-32-BZ"), new Region("IT-32-TN")), region.getSubRegions());
	}

	@Test
	void testObjectMapper() throws Exception {
		Assertions.assertEquals("[1,2,3]", objectMapper.writeValueAsString(ImmutableSet.of(1, 2, 3)));
	}
}
